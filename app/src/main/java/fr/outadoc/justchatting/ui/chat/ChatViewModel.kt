package fr.outadoc.justchatting.ui.chat

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.model.chat.ChatCommand
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.CheerEmote
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.chat.HostModeState
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.PointReward
import fr.outadoc.justchatting.model.chat.RecentEmote
import fr.outadoc.justchatting.model.chat.RoomStateDelta
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
import fr.outadoc.justchatting.util.roundUpOddToEven
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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val repository: TwitchService,
    private val emotesRepository: EmotesRepository,
    private val chatConnectionPool: ChatConnectionPool,
    private val chatEntryMapper: ChatEntryMapper,
    private val chatPreferencesRepository: ChatPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock
) : BaseViewModel() {

    private val defaultScope = viewModelScope + Dispatchers.Default + CoroutineName("defaultScope")
    private val inputScope = viewModelScope + Dispatchers.Main + CoroutineName("inputScope")

    sealed class Action {
        data class AddMessages(val messages: List<ChatCommand>) : Action()
        data class ChangeRecentEmotes(val recentEmotes: List<RecentEmote>) : Action()
        data class ChangeRoomState(val delta: RoomStateDelta) : Action()
        data class ChangeHostModeState(val hostModeState: HostModeState) : Action()
        data class ChangeUserState(val userState: UserState) : Action()
        data class LoadEmotes(val channelId: String) : Action()
        data class LoadChat(val channelLogin: String) : Action()
        object LoadStreamDetails : Action()
    }

    sealed class State {
        object Initial : State()

        @Immutable
        data class Chatting(
            val user: User,
            val appUser: AppUser,
            val stream: Stream? = null,
            val channelBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val chatMessages: PersistentList<ChatEntry> = persistentListOf(),
            val chatters: PersistentSet<Chatter> = persistentSetOf(),
            val cheerEmotes: ImmutableList<CheerEmote> = persistentListOf(),
            val globalBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val lastSentMessageInstant: Instant? = null,
            val twitchEmotes: ImmutableSet<EmoteSetItem> = persistentSetOf(),
            val otherEmotes: ImmutableSet<EmoteSetItem> = persistentSetOf(),
            val recentEmotes: List<RecentEmote> = emptyList(),
            val userState: UserState = UserState(),
            val roomState: RoomState = RoomState(),
            val hostModeState: HostModeState? = null,
            val maxAdapterCount: Int
        ) : State() {

            val allEmotes: ImmutableSet<Emote>
                get() = (twitchEmotes + otherEmotes)
                    .filterIsInstance<EmoteSetItem.Emote>()
                    .map { it.emote }
                    .distinctBy { it.name }
                    .toImmutableSet()

            val allEmotesMap: ImmutableMap<String, Emote>
                get() = allEmotes.associateBy { emote -> emote.name }
                    .plus(cheerEmotes.associateBy { emote -> emote.name })
                    .toImmutableMap()

            val availableRecentEmotes: ImmutableSet<EmoteSetItem>
                get() = recentEmotes.filter { recentEmote -> recentEmote.name in allEmotesMap }
                    .map { recentEmote -> EmoteSetItem.Emote(recentEmote) }
                    .toImmutableSet()

            val messagePostConstraint: MessagePostConstraint?
                get() = lastSentMessageInstant?.let {
                    if (roomState.slowModeDuration?.isPositive() != true) {
                        null
                    } else {
                        MessagePostConstraint(
                            lastMessageSentAt = it,
                            slowModeDuration = roomState.slowModeDuration
                        )
                    }
                }
        }
    }

    sealed class InputAction {
        data class AppendChatter(val chatter: Chatter, val autocomplete: Boolean) : InputAction()
        data class AppendEmote(val emote: Emote, val autocomplete: Boolean) : InputAction()
        data class ChangeMessageInput(val message: TextFieldValue) : InputAction()
        data class ReplyToMessage(val chatEntry: ChatEntry? = null) : InputAction()
        data class Submit(val screenDensity: Float, val isDarkTheme: Boolean) : InputAction()
    }

    data class InputState(
        val inputMessage: TextFieldValue = TextFieldValue(),
        val replyingTo: ChatEntry? = null
    ) {
        val previousWord: CharSequence
            get() = inputMessage
                .getTextBeforeSelection(inputMessage.text.length)
                .takeLastWhile { it != ' ' }
    }

    private val actions = MutableSharedFlow<Action>(extraBufferCapacity = 16)
    val state: StateFlow<State> =
        actions
            .runningFold(State.Initial) { state: State, action -> action.reduce(state) }
            .debounce(100.milliseconds)
            .stateIn(
                scope = defaultScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = State.Initial
            )

    private val inputActions = MutableSharedFlow<InputAction>()
    val inputState: StateFlow<InputState> =
        inputActions
            .runningFold(InputState()) { state: InputState, action -> action.reduce(state) }
            .stateIn(
                scope = inputScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = InputState()
            )

    init {
        defaultScope.launch {
            while (isActive) {
                delay(5.minutes)
                actions.emit(Action.LoadStreamDetails)
            }
        }

        state.filterIsInstance<State.Chatting>()
            .map { state -> state.user }
            .distinctUntilChanged()
            .onEach { user ->
                actions.emit(Action.LoadEmotes(user.id))
                actions.emit(Action.LoadStreamDetails)
            }
            .launchIn(defaultScope)

        state.filterIsInstance<State.Chatting>()
            .map { state -> state.user }
            .distinctUntilChanged()
            .flatMapLatest { user -> chatConnectionPool.start(user.id, user.login) }
            .map { command ->
                when (command) {
                    is PingCommand -> null
                    is ChatMessage,
                    is PointReward,
                    is Command -> Action.AddMessages(listOf(command))

                    is HostModeState -> Action.ChangeHostModeState(command)
                    is RoomStateDelta -> Action.ChangeRoomState(command)
                    is UserState -> Action.ChangeUserState(command)
                }
            }
            .filterNotNull()
            .onEach { action -> actions.emit(action) }
            .launchIn(defaultScope)

        state.filterIsInstance<State.Chatting>()
            .distinctUntilChanged { _, _ -> true }
            .flatMapLatest { emotesRepository.loadRecentEmotes() }
            .onEach { recentEmotes -> actions.emit(Action.ChangeRecentEmotes(recentEmotes)) }
            .launchIn(defaultScope)
    }

    fun loadChat(channelLogin: String) {
        defaultScope.launch {
            actions.emit(Action.LoadChat(channelLogin))
        }
    }

    fun onReplyToMessage(entry: ChatEntry?) {
        inputScope.launch {
            inputActions.emit(InputAction.ReplyToMessage(entry))
        }
    }

    fun onMessageInputChanged(message: TextFieldValue) {
        inputScope.launch {
            inputActions.emit(InputAction.ChangeMessageInput(message))
        }
    }

    fun appendEmote(emote: Emote, autocomplete: Boolean) {
        defaultScope.launch {
            inputActions.emit(InputAction.AppendEmote(emote, autocomplete))
        }
    }

    fun appendChatter(chatter: Chatter, autocomplete: Boolean) {
        inputScope.launch {
            inputActions.emit(InputAction.AppendChatter(chatter, autocomplete))
        }
    }

    fun submit(screenDensity: Float, isDarkTheme: Boolean) {
        inputScope.launch {
            inputActions.emit(InputAction.Submit(screenDensity, isDarkTheme))
        }
    }

    private suspend fun Action.reduce(state: State): State {
        return when (this) {
            is Action.AddMessages -> reduce(state)
            is Action.ChangeHostModeState -> reduce(state)
            is Action.ChangeRecentEmotes -> reduce(state)
            is Action.ChangeRoomState -> reduce(state)
            is Action.ChangeUserState -> reduce(state)
            is Action.LoadChat -> reduce(state)
            is Action.LoadEmotes -> reduce(state)
            is Action.LoadStreamDetails -> reduce(state)
        }
    }

    private suspend fun Action.LoadChat.reduce(state: State): State {
        if (state is State.Chatting && state.user.login == channelLogin) return state

        return State.Chatting(
            user = repository.loadUsersByLogin(logins = listOf(channelLogin))
                ?.firstOrNull()
                ?: error("User not loaded"),
            appUser = userPreferencesRepository.appUser.first() as AppUser.LoggedIn,
            chatters = persistentSetOf(Chatter(channelLogin)),
            maxAdapterCount = chatPreferencesRepository.messageLimit.first()
        )
    }

    @Suppress("UnusedReceiverParameter")
    private suspend fun Action.LoadStreamDetails.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            stream = repository.loadStreamWithUser(channelId = state.user.id)
        )
    }

    private suspend fun Action.LoadEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state

        return withContext(Dispatchers.IO) {
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
                .takeIf { it.isNotEmpty() }
                ?.flatMap { (group, emotes) ->
                    listOf(EmoteSetItem.Header(group)) +
                        emotes.map { emote -> EmoteSetItem.Emote(emote) }
                }
                ?.toPersistentSet()

            val cheerEmotes = try {
                repository.loadCheerEmotes(userId = channelId).toPersistentList()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cheermotes for channel $channelId", e)
                null
            }

            state.copy(
                cheerEmotes = cheerEmotes ?: state.cheerEmotes,
                otherEmotes = otherEmotes ?: state.otherEmotes,
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
            state.chatMessages
                .addAll(messages.mapNotNull(chatEntryMapper::map))
                .distinct()
                .toPersistentList()

        // We alternate the background of each chat row.
        // If we remove just one item, the backgrounds will shift, so we always need to remove
        // an even number of items.
        val maxCount =
            state.maxAdapterCount.roundUpOddToEven() + if (newMessages.size.isOdd) 1 else 0

        return state.copy(
            chatMessages = newMessages
                .takeLast(maxCount)
                .toPersistentList(),
            lastSentMessageInstant = lastSentMessageInstant
                ?: state.lastSentMessageInstant,
            chatters = state.chatters.addAll(newChatters)
        )
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
        return state.copy(
            roomState = RoomState(
                isEmoteOnly = delta.isEmoteOnly ?: state.roomState.isEmoteOnly,
                isSubOnly = delta.isSubOnly ?: state.roomState.isSubOnly,
                minFollowDuration = delta.minFollowDuration ?: state.roomState.minFollowDuration,
                uniqueMessagesOnly = delta.uniqueMessagesOnly ?: state.roomState.uniqueMessagesOnly,
                slowModeDuration = delta.slowModeDuration ?: state.roomState.slowModeDuration
            )
        )
    }

    private fun Action.ChangeRecentEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(recentEmotes = recentEmotes)
    }

    private fun Action.ChangeHostModeState.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(hostModeState = hostModeState)
    }

    private suspend fun InputAction.reduce(state: InputState): InputState {
        return when (this) {
            is InputAction.AppendChatter -> reduce(state)
            is InputAction.AppendEmote -> reduce(state)
            is InputAction.ChangeMessageInput -> reduce(state)
            is InputAction.ReplyToMessage -> reduce(state)
            is InputAction.Submit -> reduce(state)
        }
    }

    private suspend fun InputAction.Submit.reduce(inputState: InputState): InputState {
        if (inputState.inputMessage.text.isEmpty()) return inputState
        val state = state.value as? State.Chatting ?: return inputState

        defaultScope.launch {
            val currentTime = clock.now().toEpochMilliseconds()

            chatConnectionPool.sendMessage(
                channelId = state.user.id,
                message = inputState.inputMessage.text,
                inReplyToId = inputState.replyingTo?.data?.messageId
            )

            val usedEmotes: List<RecentEmote> =
                inputState.inputMessage
                    .text
                    .split(' ')
                    .mapNotNull { word ->
                        state.allEmotesMap[word]?.let { emote ->
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
        }

        return inputState.copy(
            inputMessage = TextFieldValue(""),
            replyingTo = null
        )
    }

    private fun InputAction.ChangeMessageInput.reduce(inputState: InputState): InputState {
        return inputState.copy(inputMessage = message)
    }

    private fun InputAction.AppendEmote.reduce(inputState: InputState): InputState {
        return appendTextToInput(
            inputState = inputState,
            text = emote.name,
            replaceLastWord = autocomplete
        )
    }

    private fun InputAction.AppendChatter.reduce(inputState: InputState): InputState {
        return appendTextToInput(
            inputState = inputState,
            text = chatter.name,
            replaceLastWord = autocomplete
        )
    }

    private fun InputAction.ReplyToMessage.reduce(inputState: InputState): InputState {
        return inputState.copy(replyingTo = chatEntry)
    }

    private fun appendTextToInput(
        inputState: InputState,
        text: String,
        replaceLastWord: Boolean
    ): InputState {
        val textBefore = inputState.inputMessage
            .getTextBeforeSelection(inputState.inputMessage.text.length)
            .removeSuffix(
                if (replaceLastWord) inputState.previousWord else ""
            )

        val textAfter = inputState.inputMessage
            .getTextAfterSelection(inputState.inputMessage.text.length)

        return inputState.copy(
            inputMessage = inputState.inputMessage.copy(
                text = "${textBefore}$text $textAfter",
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
