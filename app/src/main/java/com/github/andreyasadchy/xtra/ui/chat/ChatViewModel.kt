package com.github.andreyasadchy.xtra.ui.chat

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.*
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.BaseViewModel
import com.github.andreyasadchy.xtra.ui.player.ChatReplayManager
import com.github.andreyasadchy.xtra.ui.view.chat.ChatView
import com.github.andreyasadchy.xtra.ui.view.chat.MAX_ADAPTER_COUNT
import com.github.andreyasadchy.xtra.util.SingleLiveEvent
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.chat.*
import com.github.andreyasadchy.xtra.util.nullIfEmpty
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.asReversed
import kotlin.collections.associateBy
import kotlin.collections.contains
import kotlin.collections.containsKey
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.hashSetOf
import kotlin.collections.isNotEmpty
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import com.github.andreyasadchy.xtra.model.helix.emote.Emote as TwitchEmote

class ChatViewModel @Inject constructor(
        private val repository: TwitchService,
        private val playerRepository: PlayerRepository) : BaseViewModel(), ChatView.MessageSenderCallback {

    val recentEmotes: LiveData<List<Emote>> by lazy {
        MediatorLiveData<List<Emote>>().apply {
            addSource(twitchEmotes) { twitch ->
                removeSource(twitchEmotes)
                addSource(_otherEmotes) { other ->
                    removeSource(_otherEmotes)
                    addSource(playerRepository.loadRecentEmotes()) { recent ->
                        value = recent.filter { (twitch.contains<Emote>(it) || other.contains(it)) }
                    }
                }
            }
        }
    }
    val twitchEmotes by lazy { playerRepository.loadEmotes() }
    private val _otherEmotes = MutableLiveData<List<Emote>>()
    val otherEmotes: LiveData<List<Emote>>
        get() = _otherEmotes

    val cheerEmotes = MutableLiveData<List<CheerEmote>>()
    var emoteSetsAdded = false
    var emoteSets: List<String>? = null
    val emotesFromSets = MutableLiveData<List<Emote>>()
    val roomState = MutableLiveData<RoomState>()
    val command = MutableLiveData<Command>()

    private val _chatMessages by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = Collections.synchronizedList(ArrayList(MAX_ADAPTER_COUNT + 1)) }
    }
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages
    private val _newMessage by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage

    var globalBadges = MutableLiveData<TwitchBadgesResponse>()
    var channelBadges = MutableLiveData<TwitchBadgesResponse>()

    private var chat: ChatController? = null

    private val _newChatter by lazy { SingleLiveEvent<Chatter>() }
    val newChatter: LiveData<Chatter>
        get() = _newChatter

    val chatters: Collection<Chatter>
        get() = (chat as LiveChatController).chatters.values

    fun startLive(user: User, useHelix: Boolean, helixClientId: String?, gqlClientId: String, channelId: String?, channelLogin: String?, channelName: String?) {
        if (chat == null && channelLogin != null && channelName != null) {
            chat = LiveChatController(user, channelLogin, channelName)
            if (channelId != null) {
                init(useHelix, helixClientId, gqlClientId, user.token, channelId)
            }
        }
    }

    fun startReplay(user: User, useHelix: Boolean, helixClientId: String?, gqlClientId: String, channelId: String?, videoId: String, startTime: Double, getCurrentPosition: () -> Double) {
        if (chat == null) {
            chat = VideoChatController(gqlClientId, videoId, startTime, getCurrentPosition)
            if (channelId != null) {
                init(useHelix, helixClientId, gqlClientId, user.token, channelId)
            }
        }
    }

    fun start() {
        chat?.start()
    }

    fun stop() {
        chat?.pause()
    }

    override fun send(message: CharSequence) {
        chat?.send(message)
    }

    override fun onCleared() {
        chat?.stop()
        super.onCleared()
    }

    override fun addEmoteSets(clientId: String?, userToken: String?) {
        chat?.addEmoteSets(clientId, userToken)
    }

    private fun init(useHelix: Boolean, helixClientId: String?, gqlClientId: String, token: String?, channelId: String) {
        chat?.start()
        viewModelScope.launch {
            if (globalBadges.value == null) {
                try {
                    globalBadges.postValue(playerRepository.loadGlobalBadges())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load global badges", e)
                }
            }
            try {
                channelBadges.postValue(playerRepository.loadChannelBadges(channelId))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load badges for channel $channelId", e)
            }
            val list = mutableListOf<Emote>()
            globalStvEmotes.also {
                if (it != null) {
                    list.addAll(it)
                } else {
                    try {
                        val emotes = playerRepository.loadGlobalStvEmotes().body()?.emotes
                        if (emotes != null) {
                            globalStvEmotes = emotes
                            list.addAll(emotes)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load global 7tv emotes", e)
                    }
                }
            }
            try {
                val channelStv = playerRepository.loadStvEmotes(channelId)
                channelStv.body()?.emotes?.let(list::addAll)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load 7tv emotes for channel $channelId", e)
            }
            globalBttvEmotes.also {
                if (it != null) {
                    list.addAll(it)
                } else {
                    try {
                        val emotes = playerRepository.loadGlobalBttvEmotes().body()?.emotes
                        if (emotes != null) {
                            globalBttvEmotes = emotes
                            list.addAll(emotes)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load global BTTV emotes", e)
                    }
                }
            }
            try {
                val channelBttv = playerRepository.loadBttvEmotes(channelId)
                channelBttv.body()?.emotes?.let(list::addAll)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load BTTV emotes for channel $channelId", e)
            }
            globalFfzEmotes.also {
                if (it != null) {
                    list.addAll(it)
                } else {
                    try {
                        val emotes = playerRepository.loadBttvGlobalFfzEmotes().body()?.emotes
                        if (emotes != null) {
                            globalFfzEmotes = emotes
                            list.addAll(emotes)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load global FFZ emotes", e)
                    }
                }
            }
            try {
                val channelFfz = playerRepository.loadBttvFfzEmotes(channelId)
                channelFfz.body()?.emotes?.let(list::addAll)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load FFZ emotes for channel $channelId", e)
            }
            (chat as? LiveChatController)?.addEmotes(list)
            _otherEmotes.postValue(list)
            try {
                val get = if (useHelix) repository.loadCheerEmotes(helixClientId, token, channelId)
                else repository.loadCheerEmotesGQL(gqlClientId, channelId)
                if (get != null) {
                    cheerEmotes.postValue(get!!)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cheermotes for channel $channelId", e)
            }
        }
    }

    private inner class LiveChatController(
            private val user: User,
            private val channelName: String,
            displayName: String) : ChatController() {

        private var chat: LiveChatThread? = null
        private val allEmotesMap = mutableMapOf<String, Emote>()
        private var localEmotesObserver: Observer<List<TwitchEmote>>? = null

        val chatters = ConcurrentHashMap<String?, Chatter>()

        init {
            if (user is LoggedIn) {
                localEmotesObserver = Observer<List<TwitchEmote>> { addEmotes(it) }.also(twitchEmotes::observeForever)
            }
            chatters[displayName] = Chatter(displayName)
        }

        override fun send(message: CharSequence) {
            chat?.send(message)
            val usedEmotes = hashSetOf<RecentEmote>()
            val currentTime = System.currentTimeMillis()
            message.split(' ').forEach { word ->
                allEmotesMap[word]?.let { usedEmotes.add(RecentEmote(word, it.url, currentTime)) }
            }
            if (usedEmotes.isNotEmpty()) {
                playerRepository.insertRecentEmotes(usedEmotes)
            }
        }

        override fun start() {
            pause()
            chat = TwitchApiHelper.startChat(channelName, user.name.nullIfEmpty(), user.token.nullIfEmpty(), this, this, this, this)
        }

        override fun pause() {
            chat?.disconnect()
        }

        override fun stop() {
            pause()
            localEmotesObserver?.let(twitchEmotes::removeObserver)
        }

        override fun onMessage(message: ChatMessage) {
            super.onMessage(message)
            if (!chatters.containsKey(message.displayName)) {
                val chatter = Chatter(message.displayName)
                chatters[message.displayName] = chatter
                _newChatter.postValue(chatter)
            }
        }

        override fun addEmoteSets(clientId: String?, userToken: String?) {
            if (!emoteSetsAdded) {
                viewModelScope.launch {
                    val emotes = mutableListOf<Emote>()
                    emoteSets?.asReversed()?.forEach {
                        try {
                            val list = repository.loadEmotesFromSet(clientId, userToken, it)
                            if (list != null) {
                                emotes.addAll(list)
                            }
                        } catch (e: Exception) {
                        }
                    }
                    if (emotes.isNotEmpty()) {
                        emoteSetsAdded = true
                        emotesFromSets.value = emotes
                    }
                }
            }
        }

        override fun onUserState(list: List<String>?) {
            emoteSets = list
        }

        override fun onRoomState(list: RoomState) {
            roomState.postValue(list)
        }

        override fun onCommand(list: Command) {
            command.postValue(list)
        }

        fun addEmotes(list: List<Emote>) {
            if (user is LoggedIn) {
                allEmotesMap.putAll(list.associateBy { it.name })
            }
        }
    }

    private inner class VideoChatController(
            private val clientId: String,
            private val videoId: String,
            private val startTime: Double,
            private val getCurrentPosition: () -> Double) : ChatController() {

        private var chatReplayManager: ChatReplayManager? = null

        override fun send(message: CharSequence) {

        }

        override fun start() {
            stop()
            chatReplayManager = ChatReplayManager(clientId, repository, videoId, startTime, getCurrentPosition, this, { _chatMessages.postValue(ArrayList()) }, viewModelScope)
        }

        override fun pause() {
            chatReplayManager?.stop()
        }

        override fun stop() {
            chatReplayManager?.stop()
        }

        override fun addEmoteSets(clientId: String?, userToken: String?) {
        }

        override fun onUserState(list: List<String>?) {
        }

        override fun onRoomState(list: RoomState) {
        }

        override fun onCommand(list: Command) {
        }
    }

    private abstract inner class ChatController : OnChatMessageReceivedListener, OnUserStateReceivedListener, OnRoomStateReceivedListener, OnCommandReceivedListener {
        abstract fun send(message: CharSequence)
        abstract fun start()
        abstract fun pause()
        abstract fun stop()
        abstract fun addEmoteSets(clientId: String?, userToken: String?)

        override fun onMessage(message: ChatMessage) {
            _chatMessages.value!!.add(message)
            _newMessage.postValue(message)
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"

        private var globalStvEmotes: List<StvEmote>? = null
        private var globalBttvEmotes: List<BttvEmote>? = null
        private var globalFfzEmotes: List<FfzEmote>? = null
    }
}