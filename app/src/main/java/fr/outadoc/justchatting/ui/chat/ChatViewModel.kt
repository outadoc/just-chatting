package fr.outadoc.justchatting.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.model.chat.ChatCommand
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.CheerEmote
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.chat.LiveChatMessage
import fr.outadoc.justchatting.model.chat.RecentEmote
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.TwitchBadge
import fr.outadoc.justchatting.model.chat.TwitchEmote
import fr.outadoc.justchatting.model.chat.UserState
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.AuthPreferencesRepository
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.repository.PlayerRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import fr.outadoc.justchatting.ui.common.BaseViewModel
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntryMapper
import fr.outadoc.justchatting.util.chat.MessageListenerImpl
import fr.outadoc.justchatting.util.chat.OnChatMessageReceivedListener
import fr.outadoc.justchatting.util.chat.OnRoomStateReceivedListener
import fr.outadoc.justchatting.util.chat.OnUserStateReceivedListener
import fr.outadoc.justchatting.util.combineWith
import fr.outadoc.justchatting.util.isOdd
import fr.outadoc.justchatting.util.nullIfEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.LinkedList
import kotlin.collections.component1
import kotlin.collections.component2

class ChatViewModel(
    private val repository: TwitchService,
    private val playerRepository: PlayerRepository,
    private val chatMessageParser: ChatMessageParser,
    private val chatEntryMapper: ChatEntryMapper,
    private val chatPreferencesRepository: ChatPreferencesRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock
) : BaseViewModel() {

    data class State(
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
        val roomState: RoomState = RoomState()
    ) {
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

    private val _state = MutableStateFlow(State())
    val state: LiveData<State> = playerRepository.loadRecentEmotes()
        .combineWith(_state.asLiveData()) { recentEmotes, state ->
            state.copy(
                recentEmotes = recentEmotes
                    .filter { recentEmote -> state.allEmotesMap.containsKey(recentEmote.name) }
                    .map { emote -> EmoteSetItem.Emote(emote) }
                    .toSet()
            )
        }

    private var chatController: ChatController? = null

    fun startLive(
        channelId: String,
        channelLogin: String,
        channelName: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val user =
                userPreferencesRepository.user.first() as? fr.outadoc.justchatting.model.User.LoggedIn
                    ?: return@launch

            if (chatController == null) {
                chatController = LiveChatController(
                    user = user,
                    channelId = channelId,
                    channelLogin = channelLogin,
                    clock = clock,
                    coroutineScope = viewModelScope,
                    chatMessageParser = chatMessageParser,
                    chatStateListener = ChatStateListener(
                        user = user,
                        channelId = channelId,
                        displayName = channelName,
                        helixClientId = authPreferencesRepository.helixClientId.first(),
                        maxAdapterCount = chatPreferencesRepository.messageLimit.first()
                    )
                )

                val enableRecentMsg = chatPreferencesRepository.enableRecentMsg.first()
                val recentMsgLimit = chatPreferencesRepository.recentMsgLimit.first()

                launch {
                    if (enableRecentMsg && recentMsgLimit > 0) {
                        loadRecentMessages(
                            user = user,
                            maxAdapterCount = chatPreferencesRepository.messageLimit.first(),
                            channelLogin = channelLogin,
                            recentMsgLimit = recentMsgLimit
                        )
                    }
                }

                chatController?.start()
                loadEmotes(channelId)
            }
        }
    }

    private suspend fun loadRecentMessages(
        user: fr.outadoc.justchatting.model.User,
        channelLogin: String,
        recentMsgLimit: Int,
        maxAdapterCount: Int
    ) {
        try {
            playerRepository.loadRecentMessages(channelLogin, recentMsgLimit)
                .body()
                ?.messages
                ?.let { messages ->
                    onMessages(
                        user = user,
                        maxAdapterCount = maxAdapterCount,
                        messages = messages,
                        append = false
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load recent messages for channel $channelLogin", e)
        }
    }

    private suspend fun loadEmotes(channelId: String) = coroutineScope {
        withContext(Dispatchers.Default) {
            val globalBadges = async {
                try {
                    playerRepository.loadGlobalBadges().body()?.badges
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global badges", e)
                    null
                }
            }

            val channelBadges = async {
                try {
                    playerRepository.loadChannelBadges(channelId).body()?.badges
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load badges for channel $channelId", e)
                    null
                }
            }

            val stvEmotes = async {
                try {
                    playerRepository.loadStvEmotes(channelId).body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load 7tv emotes for channel $channelId", e)
                    null
                }.orEmpty()
            }

            val bttvEmotes = async {
                try {
                    playerRepository.loadBttvEmotes(channelId).body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load BTTV emotes for channel $channelId", e)
                    null
                }.orEmpty()
            }

            val ffzEmotes = async {
                try {
                    playerRepository.loadBttvFfzEmotes(channelId).body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load FFZ emotes for channel $channelId", e)
                    null
                }.orEmpty()
            }

            val globalStv = async {
                try {
                    playerRepository.loadGlobalStvEmotes().body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global 7tv emotes", e)
                    null
                }.orEmpty()
            }

            val globalBttv = async {
                try {
                    playerRepository.loadGlobalBttvEmotes().body()?.emotes
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global BTTV emotes", e)
                    null
                }.orEmpty()
            }

            val globalFfz = async {
                try {
                    playerRepository.loadBttvGlobalFfzEmotes().body()?.emotes
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

            _state.update { state ->
                state.copy(
                    cheerEmotes = cheerEmotes ?: state.cheerEmotes,
                    otherEmotes = otherEmotes,
                    channelBadges = channelBadges.await() ?: state.channelBadges,
                    globalBadges = globalBadges.await() ?: state.globalBadges
                )
            }
        }
    }

    private fun onMessages(
        user: fr.outadoc.justchatting.model.User,
        maxAdapterCount: Int,
        messages: List<ChatCommand>,
        append: Boolean = true
    ) {
        _state.update { state ->
            // Note that this is the last message we've sent
            val lastSentMessageInstant: Instant? =
                messages.filterIsInstance<LiveChatMessage>()
                    .lastOrNull { message ->
                        message.userId != null && message.userId == user.id
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
            val maxCount = maxAdapterCount + if (newMessages.size.isOdd) 1 else 0

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
            chatController?.send(message = message)

            val currentTime = clock.now().toEpochMilliseconds()
            val usedEmotes: List<RecentEmote> =
                message.split(' ').mapNotNull { word ->
                    state.value?.allEmotesMap?.get(word)?.let { emote ->
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

            playerRepository.insertRecentEmotes(usedEmotes)
        }
    }

    inner class ChatStateListener(
        private val user: fr.outadoc.justchatting.model.User,
        private val helixClientId: String?,
        private val channelId: String?,
        private val maxAdapterCount: Int,
        displayName: String
    ) : OnUserStateReceivedListener,
        OnRoomStateReceivedListener,
        OnChatMessageReceivedListener {

        val messageListener = MessageListenerImpl(
            callback = this,
            callbackUserState = this,
            callbackRoomState = this
        )

        init {
            _state.update { state ->
                state.copy(
                    chatters = state.chatters + Chatter(displayName)
                )
            }
        }

        override fun onMessage(message: ChatCommand) {
            onMessages(
                user = user,
                maxAdapterCount = maxAdapterCount,
                messages = listOf(message)
            )
        }

        override fun onUserState(userState: UserState) {
            if (helixClientId == null || user.helixToken?.nullIfEmpty() == null) {
                return
            }

            if (state.value?.userState != userState) {
                viewModelScope.launch(Dispatchers.Default) {
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
                                    .mapNotNull { it.ownerId }
                                    .toSet()
                                    .mapNotNull {
                                        it.toLongOrNull()
                                            ?.takeIf { id -> id > 0 }
                                            ?.toString()
                                    }
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                            .orEmpty()
                            .associateBy { user -> user.id!! }

                    val groupedChannelEmotes: Map<String?, List<TwitchEmote>> =
                        emotes.filter { emote -> emote.ownerId == channelId }
                            .groupBy { emoteOwners[channelId]?.display_name }

                    val groupedEmotes: Map<String?, List<TwitchEmote>> =
                        emotes.filter { emote -> emote.ownerId != channelId }
                            .groupBy { emote -> emoteOwners[emote.ownerId]?.display_name }

                    val sortedEmotes: Set<EmoteSetItem> =
                        (groupedChannelEmotes + groupedEmotes)
                            .flatMap { (ownerName, emotes) ->
                                listOf(EmoteSetItem.Header(title = ownerName))
                                    .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                            }
                            .toSet()

                    if (emotes.isNotEmpty()) {
                        _state.update { state ->
                            state.copy(
                                userState = userState,
                                twitchEmotes = sortedEmotes
                            )
                        }
                    }
                }
            }
        }

        override fun onRoomState(roomState: RoomState) {
            _state.update { state ->
                state.copy(roomState = roomState)
            }
        }
    }

    override fun onCleared() {
        chatController?.stop()
        super.onCleared()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
