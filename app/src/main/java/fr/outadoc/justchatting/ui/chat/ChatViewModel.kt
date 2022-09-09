package fr.outadoc.justchatting.ui.chat

import android.util.Log
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
import fr.outadoc.justchatting.model.chat.LiveChatMessage
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.PubSubPointReward
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.LinkedList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.milliseconds

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
        data class Chatting(
            val channelId: String,
            val channelLogin: String,
            val channelName: String,
            val appUser: AppUser,
            val channelBadges: List<TwitchBadge> = emptyList(),
            val chatMessages: List<ChatEntry> = LinkedList(),
            val chatters: Set<Chatter> = emptySet(),
            val cheerEmotes: List<CheerEmote> = emptyList(),
            val globalBadges: List<TwitchBadge> = emptyList(),
            val lastSentMessageInstant: Instant? = null,
            val twitchEmotes: Set<EmoteSetItem> = emptySet(),
            val otherEmotes: Set<EmoteSetItem> = emptySet(),
            val recentEmotes: Set<EmoteSetItem> = emptySet(),
            val userState: UserState = UserState(),
            val roomState: RoomState = RoomState(),
            val recentMsgLimit: Int,
            val maxAdapterCount: Int
        ) : State() {

            val allEmotes: Set<Emote> =
                (twitchEmotes + recentEmotes + otherEmotes)
                    .filterIsInstance<EmoteSetItem.Emote>()
                    .map { it.emote }
                    .toSet()

            val allEmotesMap: Map<String, Emote> =
                allEmotes.associateBy { emote -> emote.name }

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
        }
    }

    private val _state = MutableStateFlow<State>(State.Initial)

    @OptIn(FlowPreview::class)
    val state: LiveData<State> =
        emotesRepository.loadRecentEmotes()
            .combineWith(
                _state.filterIsInstance<State.Chatting>()
                    .debounce(100.milliseconds)
                    .asLiveData()
            ) { recentEmotes, state ->
                state.copy(
                    recentEmotes = recentEmotes
                        .filter { recentEmote -> state.allEmotesMap.containsKey(recentEmote.name) }
                        .map { emote -> EmoteSetItem.Emote(emote) }
                        .toSet()
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
                    chatters = setOf(Chatter(channelLogin)),
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
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global badges", e)
                    null
                }
            }

            val channelBadges = async {
                try {
                    emotesRepository.loadChannelBadges(channelId).body()?.badges
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

            val otherEmotes: Set<EmoteSetItem> =
                groups.flatMap { (group, emotes) ->
                    listOf(EmoteSetItem.Header(group))
                        .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                }.toSet()

            val cheerEmotes = try {
                repository.loadCheerEmotes(userId = channelId)
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
            is LiveChatMessage,
            is PubSubPointReward,
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

    private fun onMessages(messages: List<ChatCommand>, append: Boolean = true) {
        _state.update { s ->
            val state = s as? State.Chatting ?: return@update s

            // Note that this is the last message we've sent
            val lastSentMessageInstant: Instant? =
                messages.filterIsInstance<LiveChatMessage>()
                    .lastOrNull { message ->
                        message.userId != null && message.userId == state.appUser.id
                    }
                    ?.timestamp

            // Remember names of chatters
            val newChatters =
                messages.asSequence()
                    .filterIsInstance<ChatMessage>()
                    .mapNotNull { message -> message.userName }
                    .map { userName -> Chatter(userName) }
                    .toSet()

            val newMessages = state.chatMessages
                .toMutableList()
                .apply {
                    val messagesToAdd = messages.mapNotNull { chatEntryMapper.map(it) }
                    if (append) {
                        addAll(messagesToAdd)
                    } else {
                        addAll(0, messagesToAdd)
                    }
                }

            // We alternate the background of each chat row.
            // If we remove just one item, the backgrounds will shift, so we always need to remove
            // an even number of items.
            val maxCount = state.maxAdapterCount + if (newMessages.size.isOdd) 1 else 0

            state.copy(
                chatMessages = newMessages.takeLast(maxCount),
                lastSentMessageInstant = lastSentMessageInstant
                    ?: state.lastSentMessageInstant,
                chatters = state.chatters + newChatters
            )
        }
    }

    fun send(
        message: CharSequence,
        screenDensity: Float,
        isDarkTheme: Boolean
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            (_state.value as? State.Chatting)?.let { state ->
                chatConnectionPool.sendMessage(state.channelId, message)
            }

            val currentTime = clock.now().toEpochMilliseconds()
            val allEmotesMap = (state.value as? State.Chatting)?.allEmotesMap
            val usedEmotes: List<RecentEmote> =
                message.split(' ').mapNotNull { word ->
                    allEmotesMap?.get(word)?.let { emote ->
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

                val sortedEmotes: Set<EmoteSetItem> =
                    (groupedChannelEmotes + groupedEmotes)
                        .flatMap { (ownerName, emotes) ->
                            listOf(EmoteSetItem.Header(title = ownerName))
                                .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                        }
                        .toSet()

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
