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
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.FfzEmote
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.StvEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.BaseViewModel
import com.github.andreyasadchy.xtra.util.SingleLiveEvent
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.chat.Command
import com.github.andreyasadchy.xtra.util.chat.LiveChatThread
import com.github.andreyasadchy.xtra.util.chat.LoggedInChatThread
import com.github.andreyasadchy.xtra.util.chat.OnChatMessageReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnCommandReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnRewardReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnRoomStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnUserStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.PubSubWebSocket
import com.github.andreyasadchy.xtra.util.chat.RoomState
import com.github.andreyasadchy.xtra.util.nullIfEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.get
import kotlin.collections.set

class ChatViewModel @Inject constructor(
    private val repository: TwitchService,
    private val playerRepository: PlayerRepository
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

    val recentMessages = MutableLiveData<List<LiveChatMessage>>()
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

    var chat: ChatController? = null

    private val _newChatter by lazy { SingleLiveEvent<Chatter>() }
    val newChatter: LiveData<Chatter>
        get() = _newChatter

    val chatters: Collection<Chatter>
        get() = (chat as LiveChatController).chatters.values

    fun startLive(
        useSSl: Boolean,
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
        recentMsgLimit: Int? = null,
        maxAdapterCount: Int
    ) {
        if (chat == null && channelLogin != null && channelName != null) {
            chat = LiveChatController(
                useSSl = useSSl,
                usePubSub = usePubSub,
                user = user,
                helixClientId = helixClientId,
                channelId = channelId,
                channelLogin = channelLogin,
                displayName = channelName,
                showUserNotice = showUserNotice,
                showClearMsg = showClearMsg,
                showClearChat = showClearChat
            )
            if (channelId != null) {
                init(
                    helixClientId = helixClientId,
                    helixToken = user.helixToken?.nullIfEmpty(),
                    channelId = channelId,
                    channelLogin = channelLogin,
                    enableRecentMsg = enableRecentMsg,
                    recentMsgLimit = recentMsgLimit,
                    maxAdapterCount = maxAdapterCount
                )
            }
        }
    }

    fun start() {
        chat?.start()
    }

    fun stop() {
        chat?.pause()
    }

    fun send(
        message: CharSequence,
        animateEmotes: Boolean,
        screenDensity: Float,
        isDarkTheme: Boolean
    ) {
        chat?.send(
            message = message,
            animateEmotes = animateEmotes,
            screenDensity = screenDensity,
            isDarkTheme = isDarkTheme
        )
    }

    override fun onCleared() {
        chat?.stop()
        super.onCleared()
    }

    private fun init(
        helixClientId: String?,
        helixToken: String?,
        channelId: String,
        channelLogin: String? = null,
        enableRecentMsg: Boolean? = false,
        recentMsgLimit: Int? = null,
        maxAdapterCount: Int
    ) {
        _chatMessages.value = Collections.synchronizedList(
            ArrayList(maxAdapterCount + 1)
        )

        chat?.start()

        viewModelScope.launch {
            if (channelLogin != null && enableRecentMsg == true && recentMsgLimit != null) {
                try {
                    val get = playerRepository.loadRecentMessages(channelLogin, recentMsgLimit)
                        .body()?.messages
                    if (get != null && get.isNotEmpty()) {
                        recentMessages.postValue(get!!)
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

            (chat as? LiveChatController)?.addEmotes(
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

    inner class LiveChatController(
        private val useSSl: Boolean,
        private val usePubSub: Boolean,
        private val user: User,
        private val helixClientId: String?,
        private val channelId: String?,
        private val channelLogin: String,
        displayName: String,
        private val showUserNotice: Boolean,
        private val showClearMsg: Boolean,
        private val showClearChat: Boolean
    ) : ChatController(),
        OnUserStateReceivedListener,
        OnRoomStateReceivedListener,
        OnCommandReceivedListener,
        OnRewardReceivedListener {

        private var chat: LiveChatThread? = null
        private var loggedInChat: LoggedInChatThread? = null
        private var pubSub: PubSubWebSocket? = null

        private val allEmotesMap = mutableMapOf<String, Emote>()

        val chatters = ConcurrentHashMap<String?, Chatter>()

        init {
            chatters[displayName] = Chatter(displayName)
        }

        override fun send(
            message: CharSequence,
            animateEmotes: Boolean,
            screenDensity: Float,
            isDarkTheme: Boolean
        ) {
            if (message.toString() == "/dc" || message.toString() == "/disconnect") {
                disconnect()
                return
            }

            loggedInChat?.send(message)

            val usedEmotes = hashSetOf<RecentEmote>()
            val currentTime = System.currentTimeMillis()

            message.split(' ').forEach { word ->
                allEmotesMap[word]?.let { emote ->
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
                playerRepository.insertRecentEmotes(usedEmotes)
            }
        }

        override fun start() {
            pause()

            chat = TwitchApiHelper.startChat(
                useSSl = useSSl,
                loggedIn = user is LoggedIn,
                channelName = channelLogin,
                showUserNotice = showUserNotice,
                showClearMsg = showClearMsg,
                showClearChat = showClearChat,
                usePubSub = usePubSub,
                newMessageListener = this,
                UserStateListener = this,
                RoomStateListener = this,
                CommandListener = this,
                callbackReward = this
            )

            if (user is LoggedIn) {
                loggedInChat = TwitchApiHelper.startLoggedInChat(
                    useSSl = useSSl,
                    userName = user.login,
                    userToken = user.helixToken,
                    channelName = channelLogin,
                    showUserNotice = showUserNotice,
                    showClearMsg = showClearMsg,
                    showClearChat = showClearChat,
                    usePubSub = usePubSub,
                    newMessageListener = this,
                    UserStateListener = this,
                    RoomStateListener = this,
                    CommandListener = this,
                    callbackReward = this
                )
            }

            if (usePubSub && !channelId.isNullOrBlank()) {
                pubSub = TwitchApiHelper.startPubSub(channelId, viewModelScope, this, this)
            }
        }

        override fun pause() {
            chat?.disconnect()
            loggedInChat?.disconnect()
            pubSub?.disconnect()
        }

        override fun stop() {
            pause()
        }

        override fun onMessage(message: ChatMessage) {
            super.onMessage(message)

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
                allEmotesMap.putAll(list.associateBy { it.name })
            }
        }

        fun isActive(): Boolean? {
            return chat?.isActive
        }

        private fun disconnect() {
            if (chat?.isActive == true) {
                chat?.disconnect()
                loggedInChat?.disconnect()
                pubSub?.disconnect()
                roomState.postValue(RoomState())
                command.postValue(Command(type = "disconnect_command"))
            }
        }
    }

    abstract inner class ChatController : OnChatMessageReceivedListener {

        abstract fun send(
            message: CharSequence,
            animateEmotes: Boolean,
            screenDensity: Float,
            isDarkTheme: Boolean
        )

        abstract fun start()
        abstract fun pause()
        abstract fun stop()

        override fun onMessage(message: ChatMessage) {
            _chatMessages.value!!.add(message)
            _newMessage.postValue(message)
        }
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
