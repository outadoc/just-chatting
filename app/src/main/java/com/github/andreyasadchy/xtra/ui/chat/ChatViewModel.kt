package com.github.andreyasadchy.xtra.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.feature.irc.ChatMessageParser
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.BttvEmote
import com.github.andreyasadchy.xtra.model.chat.ChatCommand
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.Chatter
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.FfzEmote
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.StvEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
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
    private val chatEntryMapper: ChatEntryMapper
) : BaseViewModel() {

    private val _otherEmotes = MutableLiveData<List<EmoteSetItem>>()
    val otherEmotes: LiveData<List<EmoteSetItem>>
        get() = _otherEmotes

    val globalBadges = MutableLiveData<List<TwitchBadge>?>()
    val channelBadges = MutableLiveData<List<TwitchBadge>>()
    val cheerEmotes = MutableLiveData<List<CheerEmote>>()
    var emoteSetsAdded = false
    val emotesFromSets = MutableLiveData<List<EmoteSetItem>>()
    val emotesLoaded = MutableLiveData<Boolean>()
    val roomState = MutableLiveData<RoomState>()

    private val _lastSentMessageInstant = MutableLiveData<Instant?>(null)

    val messagePostConstraint: LiveData<MessagePostConstraint?> =
        _lastSentMessageInstant.combineWith(roomState) { lastSentMessageInstant, roomState ->
            if (lastSentMessageInstant == null || roomState?.slowModeDuration?.isPositive() != true) {
                null
            } else {
                MessagePostConstraint(
                    lastMessageSentAt = lastSentMessageInstant,
                    slowModeDuration = roomState.slowModeDuration
                )
            }
        }

    private val _chatMessages = MutableLiveData<List<ChatEntry>>()
    val chatMessages: LiveData<List<ChatEntry>>
        get() = _chatMessages

    private var chatStateListener: ChatStateListener? = null
    private var chatController: ChatController? = null

    private val _chatters = MutableLiveData<Set<Chatter>>()
    val chatters: LiveData<Set<Chatter>> get() = _chatters

    val recentEmotes: LiveData<List<EmoteSetItem>> =
        MediatorLiveData<List<EmoteSetItem>>().apply {
            addSource(emotesFromSets) { twitch ->
                removeSource(emotesFromSets)
                addSource(_otherEmotes) { other ->
                    removeSource(_otherEmotes)

                    val knownEmotes =
                        (twitch + other).filterIsInstance<EmoteSetItem.Emote>()
                            .map { it.emote }

                    addSource(playerRepository.loadRecentEmotes()) { recent ->
                        value = recent
                            .filter { recentEmote ->
                                knownEmotes.any { emote -> emote.name == recentEmote.name }
                            }
                            .map { emote -> EmoteSetItem.Emote(emote) }
                    }
                }
            }
        }

    private var maxAdapterCount: Int = -1

    fun startLive(
        user: User.LoggedIn,
        helixClientId: String?,
        channelId: String?,
        channelLogin: String?,
        channelName: String?,
        showUserNotice: Boolean,
        showClearMsg: Boolean,
        showClearChat: Boolean,
        enableRecentMsg: Boolean? = false,
        recentMsgLimit: Int? = null,
        maxAdapterCount: Int
    ) {
        this.maxAdapterCount = maxAdapterCount

        if (chatController == null && channelLogin != null && channelName != null) {
            val listener = ChatStateListener(
                user = user,
                helixClientId = helixClientId,
                channelId = channelId,
                displayName = channelName,
                showUserNotice = showUserNotice,
                showClearMsg = showClearMsg,
                showClearChat = showClearChat
            )

            chatStateListener = listener
            chatController = LiveChatController(
                user = user,
                channelId = channelId,
                channelLogin = channelLogin,
                chatStateListener = listener
            )

            if (channelId != null) {
                init(
                    helixClientId = helixClientId,
                    helixToken = user.helixToken?.nullIfEmpty(),
                    channelId = channelId,
                    channelLogin = channelLogin,
                    enableRecentMsg = enableRecentMsg,
                    recentMsgLimit = recentMsgLimit
                )
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            chatController?.start()
        }
    }

    fun send(
        message: CharSequence,
        animateEmotes: Boolean,
        screenDensity: Float,
        isDarkTheme: Boolean
    ) {
        chatController?.send(
            message = message,
            animateEmotes = animateEmotes,
            screenDensity = screenDensity,
            isDarkTheme = isDarkTheme
        )
    }

    override fun onCleared() {
        chatController?.stop()
        super.onCleared()
    }

    private fun init(
        helixClientId: String?,
        helixToken: String?,
        channelId: String,
        channelLogin: String? = null,
        enableRecentMsg: Boolean? = false,
        recentMsgLimit: Int? = null
    ) {
        _chatMessages.value = LinkedList()

        start()

        viewModelScope.launch {
            if (channelLogin != null && enableRecentMsg == true && recentMsgLimit != null && recentMsgLimit > 0) {
                try {
                    playerRepository.loadRecentMessages(channelLogin, recentMsgLimit)
                        .body()
                        ?.messages
                        ?.let { messages ->
                            chatStateListener?.appendMessages(messages)
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load recent messages for channel $channelLogin", e)
                }
            }

            savedGlobalBadges.also {
                if (it != null) {
                    globalBadges.value = it
                } else {
                    try {
                        val badges = playerRepository.loadGlobalBadges().body()?.badges
                        if (badges != null) {
                            savedGlobalBadges = badges
                            globalBadges.value = badges
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load global badges", e)
                    }
                }
            }

            try {
                channelBadges.postValue(
                    playerRepository.loadChannelBadges(channelId).body()?.badges
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load badges for channel $channelId", e)
            }

            val stvEmotes: List<StvEmote> = try {
                val channelStv = playerRepository.loadStvEmotes(channelId)
                channelStv.body()?.emotes
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load 7tv emotes for channel $channelId", e)
                null
            }.orEmpty()

            val bttvEmotes: List<BttvEmote> = try {
                val channelBttv = playerRepository.loadBttvEmotes(channelId)
                channelBttv.body()?.emotes
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load BTTV emotes for channel $channelId", e)
                null
            }.orEmpty()

            val ffzEmotes: List<FfzEmote> = try {
                val channelFfz = playerRepository.loadBttvFfzEmotes(channelId)
                channelFfz.body()?.emotes
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load FFZ emotes for channel $channelId", e)
                null
            }.orEmpty()

            val globalStv: List<StvEmote> = globalStvEmotes ?: try {
                playerRepository.loadGlobalStvEmotes()
                    .body()
                    ?.emotes
                    ?.also { emotes ->
                        globalStvEmotes = emotes
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load global 7tv emotes", e)
                null
            }.orEmpty()

            val globalBttv: List<BttvEmote> = globalBttvEmotes ?: try {
                playerRepository.loadGlobalBttvEmotes()
                    .body()
                    ?.emotes
                    ?.also { emotes ->
                        globalBttvEmotes = emotes
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load global BTTV emotes", e)
                null
            }.orEmpty()

            val globalFfz: List<FfzEmote> = globalFfzEmotes ?: try {
                playerRepository.loadBttvGlobalFfzEmotes()
                    .body()
                    ?.emotes
                    ?.also { emotes ->
                        globalFfzEmotes = emotes
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load global FFZ emotes", e)
                null
            }.orEmpty()

            val groups = mapOf(
                "BetterTTV" to bttvEmotes + globalBttv,
                "7TV" to stvEmotes + globalStv,
                "FrankerFaceZ" to ffzEmotes + globalFfz
            ).filterValues { emotes ->
                emotes.isNotEmpty()
            }

            chatStateListener?.addEmotes(
                groups.flatMap { (_, emotes) -> emotes }
            )

            _otherEmotes.postValue(
                groups.flatMap { (group, emotes) ->
                    listOf(EmoteSetItem.Header(group))
                        .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                }
            )

            try {
                cheerEmotes.postValue(
                    repository.loadCheerEmotes(
                        userId = channelId,
                        helixClientId = helixClientId,
                        helixToken = helixToken
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cheermotes for channel $channelId", e)
            }

            emotesLoaded.value = true
        }
    }

    inner class ChatStateListener(
        private val user: User,
        private val helixClientId: String?,
        private val channelId: String?,
        showUserNotice: Boolean,
        showClearMsg: Boolean,
        showClearChat: Boolean,
        displayName: String
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

        private val _allEmotesMap = mutableMapOf<String, Emote>()
        val allEmotesMap: Map<String, Emote> = _allEmotesMap

        init {
            _chatters.postValue(setOf(Chatter(displayName)))
        }

        suspend fun appendMessages(messages: List<ChatCommand>) =
            withContext(Dispatchers.Default) {
                messages.forEach { message ->
                    // Process side-effects
                    // Remember names of chatters
                    if (message is ChatMessage) {
                        message.userName?.let { userName ->
                            _chatters.postValue(
                                _chatters.value.orEmpty()
                                    .plusElement(Chatter(userName))
                            )
                        }
                    }

                    // Note that this is the last message we've sent
                    if (message is LiveChatMessage && message.userId != null && message.userId == user.id) {
                        _lastSentMessageInstant.postValue(message.timestamp)
                    }
                }

                val newList = _chatMessages.value.orEmpty() +
                        messages.mapNotNull { chatEntryMapper.map(it) }

                // We alternate the background of each chat row.
                // If we remove just one item, the backgrounds will shift, so we always need to remove
                // an even number of items.
                val maxCount = maxAdapterCount + if (newList.size.isOdd) 1 else 0

                _chatMessages.postValue(newList.takeLast(maxCount))
            }

        override fun onMessage(message: ChatCommand) {
            viewModelScope.launch {
                appendMessages(listOf(message))
            }
        }

        override fun onUserState(sets: List<String>?) {
            if (helixClientId == null || user.helixToken?.nullIfEmpty() == null) {
                return
            }

            if (savedEmoteSets != sets || !emoteSetsAdded) {
                viewModelScope.launch(Dispatchers.Default) {
                    loadEmotes(sets)
                }
            }
        }

        private suspend fun loadEmotes(sets: List<String>?) {
            val emotes: List<TwitchEmote> =
                sets.orEmpty()
                    .asReversed()
                    .chunked(25)
                    .flatMap { setIds ->
                        try {
                            repository.loadEmotesFromSet(
                                helixClientId = helixClientId,
                                helixToken = user.helixToken,
                                setIds = setIds
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }.orEmpty()
                    }

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
                            },
                        helixClientId = helixClientId,
                        helixToken = user.helixToken
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
                savedEmoteSets = sets
                savedEmotesFromSets = emotes
                emoteSetsAdded = true

                addEmotes(emotes)

                emotesFromSets.postValue(sortedEmotes)
            }
        }

        override fun onRoomState(list: RoomState) {
            roomState.postValue(list)
        }

        fun addEmotes(list: List<Emote>) {
            if (user is User.LoggedIn) {
                _allEmotesMap.putAll(list.associateBy { it.name })
            }
        }
    }

    inner class LiveChatController(
        private val user: User.LoggedIn,
        private val channelId: String?,
        channelLogin: String,
        private val chatStateListener: ChatStateListener
    ) : ChatController() {

        private val liveChat: LiveChatThread =
            LiveChatThread(
                scope = viewModelScope,
                channelName = channelLogin,
                listener = chatStateListener.messageListener,
                parser = chatMessageParser
            )

        private val loggedInChat: LoggedInChatThread =
            LoggedInChatThread(
                scope = viewModelScope,
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

        override fun send(
            message: CharSequence,
            animateEmotes: Boolean,
            screenDensity: Float,
            isDarkTheme: Boolean
        ) {
            loggedInChat.send(message)

            val usedEmotes = hashSetOf<RecentEmote>()
            val currentTime = Clock.System.now().toEpochMilliseconds()

            message.split(' ').forEach { word ->
                chatStateListener.allEmotesMap[word]?.let { emote ->
                    usedEmotes.add(
                        RecentEmote(
                            name = word,
                            url = emote.getUrl(
                                animate = animateEmotes,
                                screenDensity = screenDensity,
                                isDarkTheme = isDarkTheme
                            ),
                            usedAt = currentTime
                        )
                    )
                }
            }

            if (usedEmotes.isNotEmpty()) {
                viewModelScope.launch {
                    playerRepository.insertRecentEmotes(usedEmotes)
                }
            }
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

    abstract inner class ChatController {

        abstract fun send(
            message: CharSequence,
            animateEmotes: Boolean,
            screenDensity: Float,
            isDarkTheme: Boolean
        )

        abstract suspend fun start()
        abstract fun stop()
    }

    companion object {
        private const val TAG = "ChatViewModel"

        private var savedEmoteSets: List<String>? = null
        private var savedEmotesFromSets: List<Emote>? = null
        private var savedGlobalBadges: List<TwitchBadge>? = null
        private var globalStvEmotes: List<StvEmote>? = null
        private var globalBttvEmotes: List<BttvEmote>? = null
        private var globalFfzEmotes: List<FfzEmote>? = null
    }
}
