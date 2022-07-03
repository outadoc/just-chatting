package com.github.andreyasadchy.xtra.ui.chat

import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.BttvEmote
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.Chatter
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.FfzEmote
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.StvEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.BaseViewModel
import com.github.andreyasadchy.xtra.util.SingleLiveEvent
import com.github.andreyasadchy.xtra.util.chat.ChatMessageParser
import com.github.andreyasadchy.xtra.util.chat.LiveChatThread
import com.github.andreyasadchy.xtra.util.chat.LoggedInChatThread
import com.github.andreyasadchy.xtra.util.chat.MessageListenerImpl
import com.github.andreyasadchy.xtra.util.chat.OnChatMessageReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnCommandReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnRewardReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnRoomStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnUserStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.PubSubListenerImpl
import com.github.andreyasadchy.xtra.util.chat.PubSubWebSocket
import com.github.andreyasadchy.xtra.util.nullIfEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.get
import kotlin.collections.set

class ChatViewModel @Inject constructor(
    private val repository: TwitchService,
    private val playerRepository: PlayerRepository,
    private val chatMessageParser: ChatMessageParser
) : BaseViewModel() {

    val recentEmotes: LiveData<List<EmoteSetItem>> by lazy {
        MediatorLiveData<List<EmoteSetItem>>().apply {
            addSource(emotesFromSets) { twitch ->
                removeSource(emotesFromSets)
                addSource(_otherEmotes) { other ->
                    removeSource(_otherEmotes)
                    addSource(playerRepository.loadRecentEmotes()) { recent ->
                        value = recent
                            .filter { recentEmote ->
                                twitch.any { twitchEmote -> (twitchEmote as? EmoteSetItem.Emote)?.emote == recentEmote } ||
                                        other.any { otherEmote -> (otherEmote as? EmoteSetItem.Emote)?.emote == recentEmote }
                            }
                            .map { emote -> EmoteSetItem.Emote(emote) }
                    }
                }
            }
        }
    }

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
    val command = MutableLiveData<Command>()
    val reward = MutableLiveData<ChatMessage>()

    private val _chatMessages = MutableLiveData<MutableList<ChatMessage>>()
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages

    private val _newMessage by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage

    private var chatStateListener: ChatStateListener? = null
    private var chatController: ChatController? = null

    private val _newChatter by lazy { SingleLiveEvent<Chatter>() }
    val newChatter: LiveData<Chatter>
        get() = _newChatter

    val chatters: Collection<Chatter>
        get() = chatStateListener?.chatters?.values.orEmpty()

    fun startLive(
        usePubSub: Boolean,
        user: User,
        helixClientId: String?,
        channelId: String?,
        channelLogin: String?,
        channelName: String?,
        showUserNotice: Boolean,
        showClearMsg: Boolean,
        showClearChat: Boolean,
        enableRecentMsg: Boolean? = false,
        recentMsgLimit: Int? = null
    ) {
        if (chatController == null && channelLogin != null && channelName != null) {
            val listener = ChatStateListener(
                user = user,
                helixClientId = helixClientId,
                channelId = channelId,
                usePubSub = usePubSub,
                displayName = channelName,
                showUserNotice = showUserNotice,
                showClearMsg = showClearMsg,
                showClearChat = showClearChat
            )

            chatStateListener = listener
            chatController = LiveChatController(
                usePubSub = usePubSub,
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
        _chatMessages.value = Collections.synchronizedList(LinkedList())

        start()

        viewModelScope.launch {
            if (channelLogin != null && enableRecentMsg == true && recentMsgLimit != null) {
                try {
                    playerRepository.loadRecentMessages(channelLogin, recentMsgLimit)
                        .body()
                        ?.messages
                        ?.forEach { message ->
                            chatStateListener?.messageListener?.onCommand(message)
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
        usePubSub: Boolean,
        showUserNotice: Boolean,
        showClearMsg: Boolean,
        showClearChat: Boolean,
        displayName: String
    ) : OnUserStateReceivedListener,
        OnRoomStateReceivedListener,
        OnCommandReceivedListener,
        OnRewardReceivedListener,
        OnChatMessageReceivedListener {

        val messageListener = MessageListenerImpl(
            callback = this,
            callbackUserState = this,
            callbackRoomState = this,
            callbackCommand = this,
            callbackReward = this,
            showUserNotice = showUserNotice,
            showClearMsg = showClearMsg,
            showClearChat = showClearChat,
            usePubSub = usePubSub
        )

        private val _allEmotesMap = mutableMapOf<String, Emote>()
        val allEmotesMap: Map<String, Emote> = _allEmotesMap

        val chatters = ConcurrentHashMap<String?, Chatter>()

        init {
            chatters[displayName] = Chatter(displayName)
        }

        override fun onMessage(message: ChatMessage) {
            _chatMessages.value!!.add(message)
            _newMessage.postValue(message)

            if (message.userName != null && !chatters.containsKey(message.userName)) {
                val chatter = Chatter(message.userName)
                chatters[message.userName] = chatter
                _newChatter.postValue(chatter)
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

            val groupedEmotes: Map<String?, List<TwitchEmote>> =
                emotes.groupBy { emote -> emote.ownerId }

            val emoteOwners: Map<String, com.github.andreyasadchy.xtra.model.helix.user.User> =
                try {
                    repository.loadUsersById(
                        ids = groupedEmotes.keys
                            .filterNotNull()
                            .filter { id -> id.isDigitsOnly() },
                        helixClientId = helixClientId,
                        helixToken = user.helixToken
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                    .orEmpty()
                    .associateBy { user -> user.id!! }

            val channelEmotes: Map<String?, List<TwitchEmote>> =
                groupedEmotes.filter { (ownerId, _) -> ownerId == channelId }

            val sortedEmotes: List<EmoteSetItem> =
                (channelEmotes + groupedEmotes.minus(channelEmotes))
                    .flatMap { (ownerId, emotes) ->
                        val channelName = ownerId?.let { emoteOwners[it] }?.display_name
                        listOf(EmoteSetItem.Header(title = channelName))
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

        override fun onCommand(list: Command) {
            command.postValue(list)
        }

        override fun onReward(message: ChatMessage) {
            reward.postValue(message)
        }

        fun addEmotes(list: List<Emote>) {
            if (user is LoggedIn) {
                _allEmotesMap.putAll(list.associateBy { it.name })
            }
        }
    }

    inner class LiveChatController(
        private val usePubSub: Boolean,
        private val user: User,
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
            if (usePubSub && !channelId.isNullOrEmpty()) {
                PubSubWebSocket(
                    scope = viewModelScope,
                    channelId = channelId,
                    listener = PubSubListenerImpl(
                        callback = chatStateListener,
                        callbackReward = chatStateListener
                    )
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

            if (user is LoggedIn) {
                loggedInChat.start()
            }

            if (usePubSub && !channelId.isNullOrBlank()) {
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
