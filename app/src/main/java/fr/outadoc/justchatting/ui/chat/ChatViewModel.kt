package fr.outadoc.justchatting.ui.chat

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.model.chat.ChatCommand
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.CheerEmote
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.PointReward
import fr.outadoc.justchatting.model.chat.RecentEmote
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.TwitchBadge
import fr.outadoc.justchatting.model.chat.TwitchEmote
import fr.outadoc.justchatting.model.chat.UserState
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.ChatConnectionPool
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.repository.EmotesRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import fr.outadoc.justchatting.ui.common.BaseViewModel
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntryMapper
import fr.outadoc.justchatting.util.isOdd
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.collections.component1
import kotlin.collections.component2

class ChatViewModel(
    private val repository: TwitchService,
    private val emotesRepository: EmotesRepository,
    private val chatConnectionPool: ChatConnectionPool,
    private val chatEntryMapper: ChatEntryMapper,
    private val chatPreferencesRepository: ChatPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock
) : BaseViewModel() {

    sealed class Action {
        data class AddMessages(val messages: List<ChatCommand>) : Action()
        data class AppendChatter(val chatter: Chatter, val autocomplete: Boolean) : Action()
        data class AppendEmote(val emote: Emote, val autocomplete: Boolean) : Action()
        data class ChangeMessageInput(val message: TextFieldValue) : Action()
        data class ChangeRecentEmotes(val recentEmotes: List<RecentEmote>) : Action()
        data class ChangeRoomState(val roomState: RoomState) : Action()
        data class ChangeUserState(val userState: UserState) : Action()
        data class LoadEmotes(val channelId: String) : Action()
        data class LoadChat(val channelLogin: String) : Action()
        data class ReplyToMessage(val chatEntry: ChatEntry? = null) : Action()
        data class Submit(val screenDensity: Float, val isDarkTheme: Boolean) : Action()
    }

    sealed class State {
        object Initial : State()

        @Immutable
        data class Chatting(
            val user: User,
            val stream: Stream?,
            val appUser: AppUser,
            val channelBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val chatMessages: PersistentList<ChatEntry> = persistentListOf(),
            val chatters: PersistentSet<Chatter> = persistentSetOf(),
            val cheerEmotes: ImmutableList<CheerEmote> = persistentListOf(),
            val globalBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val lastSentMessageInstant: Instant? = null,
            val twitchEmotes: ImmutableSet<EmoteSetItem> = persistentSetOf(),
            val otherEmotes: ImmutableSet<EmoteSetItem> = persistentSetOf(),
            val recentEmotes: ImmutableSet<EmoteSetItem> = persistentSetOf(),
            val userState: UserState = UserState(),
            val roomState: RoomState = RoomState(),
            val recentMsgLimit: Int,
            val maxAdapterCount: Int,
            val inputMessage: TextFieldValue = TextFieldValue(),
            val replyingTo: ChatEntry? = null
        ) : State() {

            val allEmotes: ImmutableSet<Emote> =
                (twitchEmotes + recentEmotes + otherEmotes)
                    .filterIsInstance<EmoteSetItem.Emote>()
                    .map { it.emote }
                    .distinctBy { it.name }
                    .toImmutableSet()

            val allEmotesMap: ImmutableMap<String, Emote> =
                allEmotes.associateBy { emote -> emote.name }
                    .toImmutableMap()

            val messagePostConstraint: MessagePostConstraint? =
                lastSentMessageInstant?.let {
                    if (roomState.slowModeDuration?.isPositive() != true) {
                        null
                    } else {
                        MessagePostConstraint(
                            lastMessageSentAt = it,
                            slowModeDuration = roomState.slowModeDuration
                        )
                    }
                }

            val previousWord = inputMessage
                .getTextBeforeSelection(inputMessage.text.length)
                .takeLastWhile { it != ' ' }
        }
    }

    private val actions = MutableSharedFlow<Action>(extraBufferCapacity = 16)

    val state: LiveData<State> =
        actions
            .onEach { Log.w("ChatVM", "action: $it") }
            .scan(State.Initial) { state: State, action -> action.reduce(state) }
            .onEach { Log.w("ChatVM", "state: $it") }
            .flowOn(Dispatchers.Default)
            .asLiveData()

    fun loadChat(channelLogin: String) {
        Log.w("ChatVM", "loadChat($channelLogin)")
        viewModelScope.launch {
            Log.w("ChatVM", "actions.emit")
            actions.emit(Action.LoadChat(channelLogin))
        }
    }

    fun onReplyToMessage(entry: ChatEntry?) {
        viewModelScope.launch {
            actions.emit(Action.ReplyToMessage(entry))
        }
    }

    fun onMessageInputChanged(message: TextFieldValue) {
        viewModelScope.launch {
            actions.emit(Action.ChangeMessageInput(message))
        }
    }

    fun appendEmote(emote: Emote, autocomplete: Boolean) {
        viewModelScope.launch {
            actions.emit(Action.AppendEmote(emote, autocomplete))
        }
    }

    fun appendChatter(chatter: Chatter, autocomplete: Boolean) {
        viewModelScope.launch {
            actions.emit(Action.AppendChatter(chatter, autocomplete))
        }
    }

    fun submit(screenDensity: Float, isDarkTheme: Boolean) {
        viewModelScope.launch {
            actions.emit(Action.Submit(screenDensity, isDarkTheme))
        }
    }

    private suspend fun Action.reduce(state: State): State {
        return when (this) {
            is Action.AddMessages -> reduce(state)
            is Action.AppendChatter -> reduce(state)
            is Action.AppendEmote -> reduce(state)
            is Action.ChangeMessageInput -> reduce(state)
            is Action.ChangeRoomState -> reduce(state)
            is Action.ChangeUserState -> reduce(state)
            is Action.LoadChat -> reduce(state)
            is Action.LoadEmotes -> reduce(state)
            is Action.ReplyToMessage -> reduce(state)
            is Action.Submit -> reduce(state)
            is Action.ChangeRecentEmotes -> reduce(state)
        }
    }

    private suspend fun Action.LoadChat.reduce(state: State): State {
        if (state is State.Chatting && state.user.login == channelLogin) return state

        val appUser = userPreferencesRepository.appUser.first() as? AppUser.LoggedIn
            ?: return state

        val user = repository.loadUsersByLogin(logins = listOf(channelLogin))
            ?.firstOrNull()
            ?: error("User not loaded")

        val stream = repository.loadStreamWithUser(channelId = user.id)

        chatConnectionPool.start(user.id, channelLogin)
            .onEach { command ->
                val action = when (command) {
                    is PingCommand -> null
                    is ChatMessage,
                    is PointReward,
                    is Command -> {
                        Action.AddMessages(listOf(command))
                    }
                    is RoomState -> {
                        Action.ChangeRoomState(command)
                    }
                    is UserState -> {
                        Action.ChangeUserState(command)
                    }
                }

                if (action != null) {
                    viewModelScope.launch {
                        actions.emit(action)
                    }
                }
            }
            .launchIn(viewModelScope)

        emotesRepository.loadRecentEmotes()
            .onEach { recentEmotes ->
                actions.emit(Action.ChangeRecentEmotes(recentEmotes))
            }
            .launchIn(viewModelScope)

        actions.emit(Action.LoadEmotes(user.id))

        return State.Chatting(
            user = user,
            stream = stream,
            appUser = appUser,
            chatters = persistentSetOf(Chatter(channelLogin)),
            recentMsgLimit = chatPreferencesRepository.recentMsgLimit.first(),
            maxAdapterCount = chatPreferencesRepository.messageLimit.first()
        )
    }

    private suspend fun Action.LoadEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state

        return coroutineScope {
            val globalBadges = async {
                try {
                    emotesRepository.loadGlobalBadges().body()?.badges
                        ?.toPersistentList()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global badges", e)
                    null
                }
            }

            val channelBadges = async {
                try {
                    emotesRepository.loadChannelBadges(channelId).body()?.badges
                        ?.toPersistentList()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load badges for channel $channelId", e)
                    null
                }
            }

            val stvEmotes = async {
                try {
                    emotesRepository.loadStvEmotes(channelId).body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load 7tv emotes for channel $channelId", e)
                    null
                }.orEmpty()
            }

            val bttvEmotes = async {
                try {
                    emotesRepository.loadBttvEmotes(channelId).body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load BTTV emotes for channel $channelId", e)
                    null
                }.orEmpty()
            }

            val ffzEmotes = async {
                try {
                    emotesRepository.loadBttvFfzEmotes(channelId).body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load FFZ emotes for channel $channelId", e)
                    null
                }.orEmpty()
            }

            val globalStv = async {
                try {
                    emotesRepository.loadGlobalStvEmotes().body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global 7tv emotes", e)
                    null
                }.orEmpty()
            }

            val globalBttv = async {
                try {
                    emotesRepository.loadGlobalBttvEmotes().body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global BTTV emotes", e)
                    null
                }.orEmpty()
            }

            val globalFfz = async {
                try {
                    emotesRepository.loadBttvGlobalFfzEmotes().body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global FFZ emotes", e)
                    null
                }.orEmpty()
            }

            val groups = mapOf(
                "BetterTTV" to bttvEmotes.await() + globalBttv.await(),
                "7TV" to stvEmotes.await() + globalStv.await(),
                "FrankerFaceZ" to ffzEmotes.await() + globalFfz.await()
            ).filterValues { emotes ->
                emotes.isNotEmpty()
            }

            val otherEmotes = groups
                .flatMap { (group, emotes) ->
                    listOf(EmoteSetItem.Header(group)) +
                            emotes.map { emote -> EmoteSetItem.Emote(emote) }
                }
                .toPersistentSet()

            val cheerEmotes = try {
                repository.loadCheerEmotes(userId = channelId).toPersistentList()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cheermotes for channel $channelId", e)
                null
            }

            state.copy(
                cheerEmotes = cheerEmotes ?: state.cheerEmotes,
                otherEmotes = otherEmotes,
                channelBadges = channelBadges.await() ?: state.channelBadges,
                globalBadges = globalBadges.await() ?: state.globalBadges
            )
        }
    }

    private fun Action.AddMessages.reduce(state: State): State {
        if (state !is State.Chatting) return state

        // Note that this is the last message we've sent
        val lastSentMessageInstant: Instant? =
            messages.filterIsInstance<ChatMessage>()
                .lastOrNull { message ->
                    message.userId != null && message.userId == state.appUser.id
                }
                ?.timestamp

        // Remember names of chatters
        val newChatters: PersistentSet<Chatter> =
            messages.asSequence()
                .filterIsInstance<ChatMessage>()
                .map { message -> Chatter(message.userName) }
                .toPersistentSet()

        val newMessages: PersistentList<ChatEntry> =
            state.chatMessages.addAll(messages.mapNotNull(chatEntryMapper::map))

        // We alternate the background of each chat row.
        // If we remove just one item, the backgrounds will shift, so we always need to remove
        // an even number of items.
        val maxCount = state.maxAdapterCount + if (newMessages.size.isOdd) 1 else 0

        return state.copy(
            chatMessages = newMessages.takeLast(maxCount).toPersistentList(),
            lastSentMessageInstant = lastSentMessageInstant
                ?: state.lastSentMessageInstant,
            chatters = state.chatters.addAll(newChatters)
        )
    }

    private suspend fun Action.Submit.reduce(state: State): State {
        if (state !is State.Chatting) return state
        if (state.inputMessage.text.isEmpty()) return state

        val currentTime = clock.now().toEpochMilliseconds()

        chatConnectionPool.sendMessage(
            channelId = state.user.id,
            message = state.inputMessage.text,
            inReplyToId = state.replyingTo?.data?.messageId
        )

        val allEmotesMap = state.allEmotesMap
        val usedEmotes: List<RecentEmote> =
            state.inputMessage
                .text
                .split(' ')
                .mapNotNull { word ->
                    allEmotesMap[word]?.let { emote ->
                        RecentEmote(
                            name = word,
                            url = emote.getUrl(
                                animate = chatPreferencesRepository.animateEmotes.first(),
                                screenDensity = screenDensity,
                                isDarkTheme = isDarkTheme
                            ),
                            usedAt = currentTime
                        )
                    }
                }

        emotesRepository.insertRecentEmotes(usedEmotes)

        return state.copy(
            inputMessage = TextFieldValue(""),
            replyingTo = null
        )
    }

    private fun Action.ChangeMessageInput.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(inputMessage = message)
    }

    private fun Action.AppendEmote.reduce(state: State): State {
        return appendTextToInput(
            state = state,
            text = emote.name,
            replaceLastWord = autocomplete
        )
    }

    private fun Action.AppendChatter.reduce(state: State): State {
        return appendTextToInput(
            state = state,
            text = chatter.name,
            replaceLastWord = autocomplete
        )
    }

    private fun Action.ReplyToMessage.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(replyingTo = chatEntry)
    }


    private suspend fun Action.ChangeUserState.reduce(state: State): State {
        if (state !is State.Chatting) return state
        if (userState == state.userState) return state

        return coroutineScope {
            val emotes: List<TwitchEmote> =
                userState.emoteSets.asReversed()
                    .chunked(25)
                    .map { setIds ->
                        async {
                            try {
                                repository.loadEmotesFromSet(setIds = setIds)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }.orEmpty()
                        }
                    }
                    .awaitAll()
                    .flatten()

            val emoteOwners: Map<String, User> =
                try {
                    repository.loadUsersById(
                        ids = emotes
                            .mapNotNull { emote -> emote.ownerId }
                            .toSet()
                            .mapNotNull { ownerId ->
                                ownerId.toLongOrNull()
                                    ?.takeIf { id -> id > 0 }
                                    ?.toString()
                            }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                    .orEmpty()
                    .associateBy { user -> user.id }

            val groupedChannelEmotes: Map<String?, List<TwitchEmote>> =
                emotes.filter { emote -> emote.ownerId == state.user.id }
                    .groupBy { emoteOwners[state.user.id]?.displayName }

            val groupedEmotes: Map<String?, List<TwitchEmote>> =
                emotes.filter { emote -> emote.ownerId != state.user.id }
                    .groupBy { emote -> emoteOwners[emote.ownerId]?.displayName }

            val sortedEmotes: PersistentSet<EmoteSetItem> =
                (groupedChannelEmotes + groupedEmotes)
                    .flatMap { (ownerName, emotes) ->
                        listOf(EmoteSetItem.Header(title = ownerName))
                            .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                    }
                    .toPersistentSet()

            state.copy(
                userState = userState,
                twitchEmotes = sortedEmotes
            )
        }
    }

    private fun Action.ChangeRoomState.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(roomState = roomState)
    }

    private fun Action.ChangeRecentEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            recentEmotes = recentEmotes
                .filter { recentEmote -> state.allEmotesMap.containsKey(recentEmote.name) }
                .map { emote -> EmoteSetItem.Emote(emote) }
                .toImmutableSet()
        )
    }

    private fun appendTextToInput(state: State, text: String, replaceLastWord: Boolean): State {
        if (state !is State.Chatting) return state

        val textBefore = state.inputMessage
            .getTextBeforeSelection(state.inputMessage.text.length)
            .removeSuffix(
                if (replaceLastWord) state.previousWord else ""
            )

        val textAfter = state.inputMessage
            .getTextAfterSelection(state.inputMessage.text.length)

        return state.copy(
            inputMessage = state.inputMessage.copy(
                text = "${textBefore}${text} $textAfter",
                selection = TextRange(
                    index = textBefore.length + text.length + 1
                )
            )
        )
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
