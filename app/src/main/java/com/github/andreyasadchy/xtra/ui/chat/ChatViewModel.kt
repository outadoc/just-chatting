package com.github.andreyasadchy.xtra.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.feature.irc.ChatMessageParser
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.ChatCommand
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.Chatter
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.repository.AuthPreferencesRepository
import com.github.andreyasadchy.xtra.repository.ChatPreferencesRepository
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import com.github.andreyasadchy.xtra.ui.common.BaseViewModel
import com.github.andreyasadchy.xtra.ui.view.chat.model.ChatEntry
import com.github.andreyasadchy.xtra.ui.view.chat.model.ChatEntryMapper
import com.github.andreyasadchy.xtra.util.chat.LiveChatThread
import com.github.andreyasadchy.xtra.util.chat.LoggedInChatThread
import com.github.andreyasadchy.xtra.util.chat.MessageListenerImpl
import com.github.andreyasadchy.xtra.util.chat.OnChatMessageReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnRoomStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnUserStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.PubSubListenerImpl
import com.github.andreyasadchy.xtra.util.chat.PubSubWebSocket
import com.github.andreyasadchy.xtra.util.combineWith
import com.github.andreyasadchy.xtra.util.isOdd
import com.github.andreyasadchy.xtra.util.nullIfEmpty
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
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2

class ChatViewModel @Inject constructor(
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
        val allEmotesMap: Map<String, Emote> = emptyMap(),
        val channelBadges: List<TwitchBadge> = emptyList(),
        val chatMessages: List<ChatEntry> = LinkedList(),
        val chatters: Set<Chatter> = emptySet(),
        val cheerEmotes: List<CheerEmote> = emptyList(),
        val emoteSets: List<String> = emptyList(),
        val emotesFromSets: List<EmoteSetItem> = emptyList(),
        val globalBadges: List<TwitchBadge> = emptyList(),
        val lastSentMessageInstant: Instant? = null,
        val otherEmotes: List<EmoteSetItem> = emptyList(),
        val recentEmotes: List<EmoteSetItem> = emptyList(),
        val roomState: RoomState = RoomState()
    ) {
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
            )
        }

    private var chatStateListener: ChatStateListener? = null
    private var chatController: ChatController? = null

    fun startLive(
        channelId: String,
        channelLogin: String,
        channelName: String
    ) {
        viewModelScope.launch {
            val user = userPreferencesRepository.user.first() as? User.LoggedIn ?: return@launch

            if (chatController == null) {
                chatStateListener = ChatStateListener(
                    user = user,
                    channelId = channelId,
                    displayName = channelName,
                    helixClientId = authPreferencesRepository.helixClientId.first(),
                    showUserNotice = chatPreferencesRepository.showUserNotice.first(),
                    showClearMsg = chatPreferencesRepository.showClearMsg.first(),
                    showClearChat = chatPreferencesRepository.showClearChat.first(),
                    maxAdapterCount = chatPreferencesRepository.messageLimit.first()
                ).also { listener ->
                    chatController = LiveChatController(
                        user = user,
                        channelId = channelId,
                        channelLogin = channelLogin,
                        chatStateListener = listener
                    )
                }

                val enableRecentMsg = chatPreferencesRepository.enableRecentMsg.first()
                val recentMsgLimit = chatPreferencesRepository.recentMsgLimit.first()

                if (enableRecentMsg && recentMsgLimit > 0) {
                    loadRecentMessages(
                        user = user,
                        maxAdapterCount = chatPreferencesRepository.messageLimit.first(),
                        channelLogin = channelLogin,
                        recentMsgLimit = recentMsgLimit
                    )
                }

                chatController?.start()
                loadEmotes(channelId)
            }
        }
    }

    private suspend fun loadRecentMessages(
        user: User,
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
                        messages = messages
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

            val otherEmotes: List<EmoteSetItem> =
                groups.flatMap { (group, emotes) ->
                    listOf(EmoteSetItem.Header(group))
                        .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                }

            val cheerEmotes = try {
                repository.loadCheerEmotes(userId = channelId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cheermotes for channel $channelId", e)
                null
            }

            _state.update { state ->
                state.copy(
                    allEmotesMap = state.allEmotesMap + groups
                        .flatMap { (_, emotes) -> emotes }
                        .associateBy { it.name },
                    cheerEmotes = cheerEmotes ?: state.cheerEmotes,
                    otherEmotes = otherEmotes,
                    channelBadges = channelBadges.await() ?: state.channelBadges,
                    globalBadges = globalBadges.await() ?: state.globalBadges
                )
            }
        }
    }

    private fun onMessages(user: User, maxAdapterCount: Int, messages: List<ChatCommand>) {
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
                    addAll(messages.mapNotNull { chatEntryMapper.map(it) })
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
        viewModelScope.launch {
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
        private val user: User,
        private val helixClientId: String?,
        private val channelId: String?,
        showUserNotice: Boolean,
        showClearMsg: Boolean,
        showClearChat: Boolean,
        displayName: String,
        private val maxAdapterCount: Int
    ) : OnUserStateReceivedListener,
        OnRoomStateReceivedListener,
        OnChatMessageReceivedListener {

        val messageListener = MessageListenerImpl(
            callback = this,
            callbackUserState = this,
            callbackRoomState = this,
            showUserNotice = showUserNotice,
            showClearMsg = showClearMsg,
            showClearChat = showClearChat
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

        override fun onUserState(sets: List<String>) {
            if (helixClientId == null || user.helixToken?.nullIfEmpty() == null) {
                return
            }

            if (state.value?.emoteSets != sets) {
                viewModelScope.launch(Dispatchers.Default) {
                    loadTwitchEmotes(sets)
                }
            }
        }

        private suspend fun loadTwitchEmotes(sets: List<String>) = coroutineScope {
            val emotes: List<TwitchEmote> =
                sets.asReversed()
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

            val emoteOwners: Map<String, com.github.andreyasadchy.xtra.model.helix.user.User> =
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

            val sortedEmotes: List<EmoteSetItem> =
                (groupedChannelEmotes + groupedEmotes)
                    .flatMap { (ownerName, emotes) ->
                        listOf(EmoteSetItem.Header(title = ownerName))
                            .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                    }

            if (emotes.isNotEmpty()) {
                _state.update { state ->
                    state.copy(
                        emoteSets = sets,
                        allEmotesMap = state.allEmotesMap.plus(
                            emotes.associateBy { emote -> emote.name }
                        ),
                        emotesFromSets = sortedEmotes
                    )
                }
            }
        }

        override fun onRoomState(roomState: RoomState) {
            _state.update { state ->
                state.copy(roomState = roomState)
            }
        }
    }

    inner class LiveChatController(
        user: User.LoggedIn,
        private val channelId: String?,
        channelLogin: String,
        chatStateListener: ChatStateListener
    ) : ChatController() {

        private val liveChat: LiveChatThread =
            LiveChatThread(
                scope = viewModelScope,
                clock = clock,
                channelName = channelLogin,
                listener = chatStateListener.messageListener,
                parser = chatMessageParser
            )

        private val loggedInChat: LoggedInChatThread =
            LoggedInChatThread(
                scope = viewModelScope,
                clock = clock,
                userLogin = user.login,
                userToken = user.helixToken,
                channelName = channelLogin,
                listener = chatStateListener.messageListener,
                parser = chatMessageParser
            )

        private val pubSub: PubSubWebSocket? =
            if (!channelId.isNullOrEmpty()) {
                PubSubWebSocket(
                    scope = viewModelScope,
                    channelId = channelId,
                    listener = PubSubListenerImpl(callback = chatStateListener)
                )
            } else null

        override fun send(message: CharSequence) {
            loggedInChat.send(message)
        }

        override suspend fun start() {
            liveChat.start()
            loggedInChat.start()

            if (!channelId.isNullOrBlank()) {
                pubSub?.start()
            }
        }

        override fun stop() {
            liveChat.disconnect()
            loggedInChat.disconnect()
            pubSub?.disconnect()
        }
    }

    override fun onCleared() {
        chatController?.stop()
        super.onCleared()
    }

    abstract inner class ChatController {

        abstract fun send(message: CharSequence)

        abstract suspend fun start()
        abstract fun stop()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
