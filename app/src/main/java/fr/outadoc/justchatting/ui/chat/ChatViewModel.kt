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
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.ChatConnectionPool
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.repository.EmotesRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import fr.outadoc.justchatting.ui.common.BaseViewModel
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntryMapper
import fr.outadoc.justchatting.util.combineWith
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    sealed class State {
        object Initial : State()

        @Immutable
        data class Chatting(
            val channelId: String,
            val channelLogin: String,
            val channelName: String,
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
            val inputMessage: TextFieldValue = TextFieldValue()
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

    private val _state = MutableStateFlow<State>(State.Initial)
    val state: LiveData<State> =
        emotesRepository.loadRecentEmotes()
            .combineWith(
                _state.filterIsInstance<State.Chatting>().asLiveData()
            ) { recentEmotes, state ->
                state.copy(
                    recentEmotes = recentEmotes
                        .filter { recentEmote -> state.allEmotesMap.containsKey(recentEmote.name) }
                        .map { emote -> EmoteSetItem.Emote(emote) }
                        .toImmutableSet()
                )
            }

    fun startLive(channelId: String, channelLogin: String, channelName: String) {
        val state = _state.value

        if (state is State.Chatting &&
            state.channelId == channelId &&
            state.channelLogin == channelLogin &&
            state.channelName == channelName
        ) return

        viewModelScope.launch(Dispatchers.Default) {
            _state.emit(
                State.Chatting(
                    channelId = channelId,
                    channelLogin = channelLogin,
                    channelName = channelName,
                    appUser = userPreferencesRepository.appUser.first() as? AppUser.LoggedIn
                        ?: return@launch,
                    chatters = persistentSetOf(Chatter(channelLogin)),
                    recentMsgLimit = chatPreferencesRepository.recentMsgLimit.first(),
                    maxAdapterCount = chatPreferencesRepository.messageLimit.first()
                )
            )

            chatConnectionPool.start(channelId, channelLogin)
                .onEach(::onCommand)
                .launchIn(viewModelScope)

            loadEmotes(channelId)
        }
    }

    private suspend fun loadEmotes(channelId: String) = coroutineScope {
        withContext(Dispatchers.Default) {
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

            _state.update {
                val state = it as? State.Chatting ?: return@update it
                state.copy(
                    cheerEmotes = cheerEmotes ?: state.cheerEmotes,
                    otherEmotes = otherEmotes,
                    channelBadges = channelBadges.await() ?: state.channelBadges,
                    globalBadges = globalBadges.await() ?: state.globalBadges
                )
            }
        }
    }

    private fun onCommand(command: ChatCommand) {
        when (command) {
            is ChatMessage,
            is PointReward,
            is Command.Ban,
            is Command.Timeout,
            is Command.ClearChat,
            is Command.ClearMessage,
            is Command.UserNotice,
            is Command.Notice,
            is Command.Join,
            is Command.Disconnect,
            is Command.SendMessageError,
            is Command.SocketError -> onMessages(listOf(command))
            is RoomState -> onRoomState(command)
            is UserState -> onUserState(command)
            PingCommand -> Unit
        }
    }

    private fun onMessages(messages: List<ChatCommand>) {
        _state.update { s ->
            val state = s as? State.Chatting ?: return@update s

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

            state.copy(
                chatMessages = newMessages.takeLast(maxCount).toPersistentList(),
                lastSentMessageInstant = lastSentMessageInstant
                    ?: state.lastSentMessageInstant,
                chatters = state.chatters.addAll(newChatters)
            )
        }
    }

    fun submit(screenDensity: Float, isDarkTheme: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { s ->
                (s as? State.Chatting)
                    ?.takeUnless { state -> state.inputMessage.text.isEmpty() }
                    ?.let { state ->
                        val currentTime = clock.now().toEpochMilliseconds()
                        chatConnectionPool.sendMessage(state.channelId, state.inputMessage.text)

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

                        state.copy(inputMessage = TextFieldValue(""))
                    } ?: s
            }
        }
    }

    fun onMessageInputChanged(message: TextFieldValue) {
        _state.update { state ->
            (state as? State.Chatting)
                ?.copy(inputMessage = message)
                ?: state
        }
    }

    fun appendEmote(emote: Emote, autocomplete: Boolean) {
        appendTextToInput(emote.name, replaceLastWord = autocomplete)
    }

    fun appendChatter(chatter: Chatter, autocomplete: Boolean) {
        appendTextToInput(chatter.name, replaceLastWord = autocomplete)
    }

    private fun appendTextToInput(text: String, replaceLastWord: Boolean) {
        _state.update { s ->
            (s as? State.Chatting)
                ?.let { state ->
                    val textBefore = state.inputMessage
                        .getTextBeforeSelection(state.inputMessage.text.length)
                        .removeSuffix(
                            if (replaceLastWord) state.previousWord else ""
                        )

                    val textAfter = state.inputMessage
                        .getTextAfterSelection(state.inputMessage.text.length)

                    state.copy(
                        inputMessage = state.inputMessage.copy(
                            text = "${textBefore}${text} $textAfter",
                            selection = TextRange(
                                index = textBefore.length + text.length + 1
                            )
                        )
                    )
                } ?: s
        }
    }

    private fun onUserState(userState: UserState) {
        viewModelScope.launch(Dispatchers.Default) {
            _state.update {
                val state = it as? State.Chatting ?: return@update it
                if (userState == state.userState) return@update state

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
                    emotes.filter { emote -> emote.ownerId == state.channelId }
                        .groupBy { emoteOwners[state.channelId]?.display_name }

                val groupedEmotes: Map<String?, List<TwitchEmote>> =
                    emotes.filter { emote -> emote.ownerId != state.channelId }
                        .groupBy { emote -> emoteOwners[emote.ownerId]?.display_name }

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
    }

    private fun onRoomState(roomState: RoomState) {
        _state.update { state ->
            (state as? State.Chatting)
                ?.copy(roomState = roomState)
                ?: state
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
