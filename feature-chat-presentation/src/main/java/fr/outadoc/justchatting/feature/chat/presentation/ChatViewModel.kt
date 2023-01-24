package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.component.chatapi.domain.model.Chatter
import fr.outadoc.justchatting.component.chatapi.domain.model.CheerEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.RecentEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.chat.data.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteSetItem
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.model.ChatMessage
import fr.outadoc.justchatting.feature.chat.data.model.Command
import fr.outadoc.justchatting.feature.chat.data.model.HostModeState
import fr.outadoc.justchatting.feature.chat.data.model.PingCommand
import fr.outadoc.justchatting.feature.chat.data.model.PointReward
import fr.outadoc.justchatting.feature.chat.data.model.RoomStateDelta
import fr.outadoc.justchatting.feature.chat.data.model.UserState
import fr.outadoc.justchatting.feature.chat.domain.ChatConnectionPool
import fr.outadoc.justchatting.utils.core.asStringOrRes
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.core.isOdd
import fr.outadoc.justchatting.utils.core.roundUpOddToEven
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ChatViewModel(
    private val twitchRepository: TwitchRepository,
    private val emotesRepository: EmotesRepository,
    private val chatConnectionPool: ChatConnectionPool,
    private val chatEntryMapper: ChatEntryMapper,
    private val preferencesRepository: PreferenceRepository,
    private val clock: Clock,
    private val emoteListSourcesProvider: EmoteListSourcesProvider
) : ViewModel() {

    private val defaultScope = viewModelScope + CoroutineName("defaultScope")
    private val inputScope = viewModelScope + CoroutineName("inputScope")

    sealed class Action {
        data class AddMessages(val messages: List<ChatCommand>) : Action()
        data class ChangeRecentEmotes(val recentEmotes: List<RecentEmote>) : Action()
        data class ChangeRoomState(val delta: RoomStateDelta) : Action()
        data class ChangeConnectionStatus(val connectionStatus: ConnectionStatus) : Action()
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
            val pickableEmotes: ImmutableList<EmoteSetItem> = persistentListOf(),
            val recentEmotes: List<RecentEmote> = emptyList(),
            val userState: UserState = UserState(),
            val roomState: RoomState = RoomState(),
            val hostModeState: HostModeState? = null,
            val connectionStatus: ConnectionStatus = ConnectionStatus(),
            val maxAdapterCount: Int
        ) : State() {

            val allEmotesMap: ImmutableMap<String, Emote>
                get() = pickableEmotes
                    .asSequence()
                    .filterIsInstance<EmoteSetItem.Emote>()
                    .map { item -> item.emote }
                    .distinctBy { emote -> emote.name }
                    .associateBy { emote -> emote.name }
                    .plus(cheerEmotes.associateBy { emote -> emote.name })
                    .toImmutableMap()

            val pickableEmotesWithRecent: ImmutableList<EmoteSetItem>
                get() = flatListOf(
                    EmoteSetItem.Header(
                        title = R.string.chat_header_recent.asStringOrRes(),
                        source = null
                    ),
                    recentEmotes
                        .filter { recentEmote -> recentEmote.name in allEmotesMap }
                        .map { recentEmote -> EmoteSetItem.Emote(recentEmote) }
                )
                    .plus(pickableEmotes)
                    .toImmutableList()

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
        data class UpdateAutoCompleteItems(val items: List<AutoCompleteItem>) : InputAction()
        data class Submit(val screenDensity: Float, val isDarkTheme: Boolean) : InputAction()
    }

    data class InputState(
        val inputMessage: TextFieldValue = TextFieldValue(),
        val replyingTo: ChatEntry? = null,
        val autoCompleteItems: List<AutoCompleteItem> = emptyList()
    )

    private val actions = MutableSharedFlow<Action>(extraBufferCapacity = 16)

    @OptIn(FlowPreview::class)
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
            .map { user -> chatConnectionPool.start(user.id, user.login) }
            .onEach { result ->
                result.commandFlow
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

                result.connectionStatus
                    .map { status -> Action.ChangeConnectionStatus(status) }
                    .onEach { action -> actions.emit(action) }
                    .launchIn(defaultScope)
            }
            .launchIn(defaultScope)

        state.filterIsInstance<State.Chatting>()
            .distinctUntilChanged { _, _ -> true }
            .flatMapLatest { emotesRepository.loadRecentEmotes() }
            .onEach { recentEmotes -> actions.emit(Action.ChangeRecentEmotes(recentEmotes)) }
            .launchIn(defaultScope)

        state.filterIsInstance<State.Chatting>()
            .distinctUntilChanged()
            .map { state -> state.allEmotesMap to state.chatters }
            .distinctUntilChanged()
            .flatMapLatest { (allEmotesMap, chatters) ->
                inputState
                    .map { inputState -> inputState.inputMessage }
                    .distinctUntilChanged()
                    .debounce(300.milliseconds)
                    .map { message ->
                        message.getTextBeforeSelection(message.text.length)
                            .takeLastWhile { it != ' ' }
                    }
                    .mapLatest { word ->
                        if (word.isBlank()) {
                            emptyList()
                        } else {
                            val emoteItems = allEmotesMap.mapNotNull { emote ->
                                if (emote.key.contains(word, ignoreCase = true)) {
                                    AutoCompleteItem.Emote(emote.value)
                                } else {
                                    null
                                }
                            }

                            val chatterItems = chatters.mapNotNull { chatter ->
                                if (chatter.name.contains(word, ignoreCase = true)) {
                                    AutoCompleteItem.User(chatter)
                                } else {
                                    null
                                }
                            }

                            emoteItems + chatterItems
                        }
                    }
                    .flowOn(Dispatchers.Default)
            }
            .onEach { autoCompleteItems ->
                inputActions.emit(
                    InputAction.UpdateAutoCompleteItems(autoCompleteItems)
                )
            }
            .launchIn(viewModelScope)
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
        logDebug<ChatViewModel> { "reduce: $this" }
        return when (this) {
            is Action.AddMessages -> reduce(state)
            is Action.ChangeConnectionStatus -> reduce(state)
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

        val prefs = preferencesRepository.currentPreferences.first()
        return State.Chatting(
            user = twitchRepository.loadUsersByLogin(logins = listOf(channelLogin))
                ?.firstOrNull()
                ?: error("User not loaded"),
            appUser = prefs.appUser as AppUser.LoggedIn,
            chatters = persistentSetOf(Chatter(channelLogin)),
            maxAdapterCount = prefs.messageLimit
        )
    }

    @Suppress("UnusedReceiverParameter")
    private suspend fun Action.LoadStreamDetails.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            stream = twitchRepository.loadStreamWithUser(channelId = state.user.id)
        )
    }

    private suspend fun Action.LoadEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state

        return withContext(Dispatchers.IO) {
            val globalBadges = async {
                try {
                    emotesRepository.loadGlobalBadges().toPersistentList()
                } catch (e: Exception) {
                    logError<ChatViewModel>(e) { "Failed to load global badges" }
                    null
                }
            }

            val channelBadges = async {
                try {
                    emotesRepository.loadChannelBadges(channelId).toPersistentList()
                } catch (e: Exception) {
                    logError<ChatViewModel>(e) { "Failed to load badges for channel $channelId" }
                    null
                }
            }

            val cheerEmotes = try {
                twitchRepository.loadCheerEmotes(userId = channelId).toPersistentList()
            } catch (e: Exception) {
                logError<ChatViewModel>(e) { "Failed to load cheermotes for channel $channelId" }
                null
            }

            val pickableEmotes = loadPickableEmotes(
                channelId = channelId,
                channelName = state.user.displayName,
                emoteSets = state.userState.emoteSets
            )

            state.copy(
                cheerEmotes = cheerEmotes ?: state.cheerEmotes,
                pickableEmotes = pickableEmotes,
                channelBadges = channelBadges.await() ?: state.channelBadges,
                globalBadges = globalBadges.await() ?: state.globalBadges
            )
        }
    }

    private suspend fun loadPickableEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>
    ): PersistentList<EmoteSetItem> {
        return coroutineScope {
            emoteListSourcesProvider.getSources()
                .map { source ->
                    async {
                        try {
                            source.getEmotes(
                                channelId = channelId,
                                channelName = channelName,
                                emoteSets = emoteSets
                            )
                        } catch (e: Exception) {
                            logError<ChatViewModel>(e) { "Failed to load emotes from source $source" }
                            emptyList()
                        }
                    }
                }
                .awaitAll()
                .flatten()
                .toPersistentList()
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

    private fun Action.ChangeConnectionStatus.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(connectionStatus = connectionStatus)
    }

    private suspend fun Action.ChangeUserState.reduce(state: State): State {
        if (state !is State.Chatting) return state

        val pickableEmotes = loadPickableEmotes(
            channelId = state.user.id,
            channelName = state.user.displayName,
            emoteSets = userState.emoteSets
        )

        return state.copy(
            userState = userState,
            pickableEmotes = pickableEmotes
        )
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
            is InputAction.UpdateAutoCompleteItems -> reduce(state)
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

            val prefs = preferencesRepository.currentPreferences.first()
            val usedEmotes: List<RecentEmote> =
                inputState.inputMessage
                    .text
                    .split(' ')
                    .mapNotNull { word ->
                        state.allEmotesMap[word]?.let { emote ->
                            RecentEmote(
                                name = word,
                                url = emote.getUrl(
                                    animate = prefs.animateEmotes,
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

    private fun InputAction.UpdateAutoCompleteItems.reduce(inputState: InputState): InputState {
        return inputState.copy(autoCompleteItems = items)
    }

    private fun appendTextToInput(
        inputState: InputState,
        text: String,
        replaceLastWord: Boolean
    ): InputState {
        val previousWord = inputState.inputMessage
            .getTextBeforeSelection(inputState.inputMessage.text.length)
            .takeLastWhile { it != ' ' }

        val textBefore = inputState.inputMessage
            .getTextBeforeSelection(inputState.inputMessage.text.length)
            .removeSuffix(
                if (replaceLastWord) previousWord else ""
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
}