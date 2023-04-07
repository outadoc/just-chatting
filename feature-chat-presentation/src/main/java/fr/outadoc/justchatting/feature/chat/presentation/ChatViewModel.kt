package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import fr.outadoc.justchatting.component.chatapi.common.PinnedMessage
import fr.outadoc.justchatting.component.chatapi.common.Poll
import fr.outadoc.justchatting.component.chatapi.common.Prediction
import fr.outadoc.justchatting.component.chatapi.common.Pronoun
import fr.outadoc.justchatting.component.chatapi.common.Raid
import fr.outadoc.justchatting.component.chatapi.domain.model.RecentEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteSetItem
import fr.outadoc.justchatting.feature.chat.domain.ChatConnectionPool
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import fr.outadoc.justchatting.utils.core.asStringOrRes
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.core.isOdd
import fr.outadoc.justchatting.utils.core.roundUpOddToEven
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ChatViewModel(
    private val clock: Clock,
    private val twitchRepository: TwitchRepository,
    private val emotesRepository: EmotesRepository,
    private val chatConnectionPool: ChatConnectionPool,
    private val preferencesRepository: PreferenceRepository,
    private val emoteListSourcesProvider: EmoteListSourcesProvider,
    private val filterAutocompleteItemsUseCase: FilterAutocompleteItemsUseCase,
    private val pronounsRepository: PronounsRepository,
) : ViewModel() {

    private val defaultScope = viewModelScope + CoroutineName("defaultScope")
    private val inputScope = viewModelScope + CoroutineName("inputScope")

    sealed class Action {
        data class AddMessages(val messages: List<ChatEvent.Message>) : Action()
        data class ChangeRecentEmotes(val recentEmotes: List<RecentEmote>) : Action()
        data class ChangeRoomState(val delta: ChatEvent.RoomStateDelta) : Action()
        data class ChangeConnectionStatus(val connectionStatus: ConnectionStatus) : Action()
        data class ChangeUserState(val userState: ChatEvent.UserState) : Action()
        data class RemoveContent(val removedContent: ChatEvent.RemoveContent) : Action()
        data class UpdatePoll(val poll: Poll) : Action()
        data class UpdatePrediction(val prediction: Prediction) : Action()
        data class UpdateRaidAnnouncement(val raid: Raid) : Action()
        data class UpdatePinnedMessage(val pinnedMessage: PinnedMessage?) : Action()
        data class AddRichEmbed(val richEmbed: ChatEvent.RichEmbed) : Action()
        data class UpdateStreamMetadata(
            val viewerCount: Int? = null,
            val streamTitle: String? = null,
            val gameName: String? = null,
        ) : Action()

        data class LoadEmotes(val channelId: String) : Action()
        data class LoadChat(val channelLogin: String) : Action()
        data class UpdateChatterPronouns(val pronouns: Map<Chatter, Pronoun?>) : Action()
        object LoadStreamDetails : Action()
    }

    @Immutable
    sealed class State {
        object Initial : State()

        data class Chatting(
            val user: User,
            val appUser: AppUser.LoggedIn,
            val stream: Stream? = null,
            val channelBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val chatMessages: PersistentList<ChatEvent.Message> = persistentListOf(),
            val chatters: PersistentSet<Chatter> = persistentHashSetOf(),
            val pronouns: PersistentMap<Chatter, Pronoun?> = persistentMapOf(),
            val cheerEmotes: PersistentMap<String, Emote> = persistentMapOf(),
            val globalBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val lastSentMessageInstant: Instant? = null,
            val pickableEmotes: ImmutableList<EmoteSetItem> = persistentListOf(),
            val richEmbeds: PersistentMap<String, ChatEvent.RichEmbed> = persistentMapOf(),
            val recentEmotes: List<RecentEmote> = emptyList(),
            val userState: ChatEvent.UserState = ChatEvent.UserState(),
            val roomState: RoomState = RoomState(),
            val ongoingEvents: OngoingEvents = OngoingEvents(),
            val removedContent: PersistentList<ChatEvent.RemoveContent> = persistentListOf(),
            val connectionStatus: ConnectionStatus = ConnectionStatus(),
            val maxAdapterCount: Int,
        ) : State() {

            val allEmotesMap: ImmutableMap<String, Emote>
                get() = pickableEmotes
                    .asSequence()
                    .filterIsInstance<EmoteSetItem.Emote>()
                    .map { item -> item.emote }
                    .distinctBy { emote -> emote.name }
                    .associateBy { emote -> emote.name }
                    .toImmutableMap()

            val pickableEmotesWithRecent: ImmutableList<EmoteSetItem>
                get() = flatListOf(
                    EmoteSetItem.Header(
                        title = R.string.chat_header_recent.asStringOrRes(),
                        source = null,
                    ),
                    recentEmotes
                        .filter { recentEmote -> recentEmote.name in allEmotesMap }
                        .map { recentEmote ->
                            EmoteSetItem.Emote(
                                Emote(
                                    name = recentEmote.name,
                                    urls = EmoteUrls(recentEmote.url),
                                ),
                            )
                        },
                )
                    .plus(pickableEmotes)
                    .toImmutableList()

            val messagePostConstraint: MessagePostConstraint?
                get() = lastSentMessageInstant?.let {
                    if (!roomState.slowModeDuration.isPositive()) {
                        null
                    } else {
                        MessagePostConstraint(
                            lastMessageSentAt = it,
                            slowModeDuration = roomState.slowModeDuration,
                        )
                    }
                }
        }
    }

    sealed class InputAction {
        data class AppendChatter(val chatter: Chatter, val autocomplete: Boolean) : InputAction()
        data class AppendEmote(val emote: Emote, val autocomplete: Boolean) : InputAction()
        data class ChangeMessageInput(val message: TextFieldValue) : InputAction()
        data class ReplyToMessage(val chatEvent: ChatEvent.Message? = null) : InputAction()
        data class UpdateAutoCompleteItems(val items: List<AutoCompleteItem>) : InputAction()
        data class Submit(val screenDensity: Float, val isDarkTheme: Boolean) : InputAction()
    }

    @Immutable
    data class InputState(
        val inputMessage: TextFieldValue = TextFieldValue(),
        val replyingTo: ChatEvent.Message? = null,
        val autoCompleteItems: List<AutoCompleteItem> = emptyList(),
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
                initialValue = State.Initial,
            )

    private val inputActions = MutableSharedFlow<InputAction>()
    val inputState: StateFlow<InputState> =
        inputActions
            .runningFold(InputState()) { state: InputState, action -> action.reduce(state) }
            .stateIn(
                scope = inputScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = InputState(),
            )

    init {
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
                            is ChatEvent.Message.Highlighted -> {
                                Action.AddMessages(listOf(command))
                            }

                            is ChatEvent.Message.Simple -> {
                                Action.AddMessages(listOf(command))
                            }

                            is ChatEvent.RoomStateDelta -> {
                                Action.ChangeRoomState(command)
                            }

                            is ChatEvent.UserState -> {
                                Action.ChangeUserState(command)
                            }

                            is ChatEvent.RemoveContent -> {
                                Action.RemoveContent(command)
                            }

                            is ChatEvent.PollUpdate -> {
                                Action.UpdatePoll(command.poll)
                            }

                            is ChatEvent.PredictionUpdate -> {
                                Action.UpdatePrediction(command.prediction)
                            }

                            is ChatEvent.BroadcastSettingsUpdate -> {
                                Action.UpdateStreamMetadata(
                                    streamTitle = command.streamTitle,
                                    gameName = command.gameName,
                                )
                            }

                            is ChatEvent.ViewerCountUpdate -> {
                                Action.UpdateStreamMetadata(
                                    viewerCount = command.viewerCount,
                                )
                            }

                            is ChatEvent.RichEmbed -> {
                                Action.AddRichEmbed(command)
                            }

                            is ChatEvent.RaidUpdate -> {
                                Action.UpdateRaidAnnouncement(
                                    raid = command.raid,
                                )
                            }

                            is ChatEvent.PinnedMessageUpdate -> {
                                Action.UpdatePinnedMessage(
                                    pinnedMessage = command.pinnedMessage,
                                )
                            }
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
            .map { state -> state.chatters - state.pronouns.keys }
            .distinctUntilChanged()
            .debounce(3.seconds)
            .map { chatters -> pronounsRepository.fillPronounsFor(chatters) }
            .onEach { pronouns -> actions.emit(Action.UpdateChatterPronouns(pronouns)) }
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
                        filterAutocompleteItemsUseCase(
                            filter = word,
                            allEmotesMap = allEmotesMap,
                            chatters = chatters,
                        )
                    }
                    .flowOn(Dispatchers.Default)
            }
            .onEach { autoCompleteItems ->
                inputActions.emit(
                    InputAction.UpdateAutoCompleteItems(autoCompleteItems),
                )
            }
            .launchIn(viewModelScope)
    }

    fun loadChat(channelLogin: String) {
        defaultScope.launch {
            actions.emit(Action.LoadChat(channelLogin))
        }
    }

    fun onReplyToMessage(entry: ChatEvent.Message?) {
        inputScope.launch {
            inputActions.emit(InputAction.ReplyToMessage(entry))
        }
    }

    fun onMessageInputChanged(message: TextFieldValue) {
        inputScope.launch {
            inputActions.emit(InputAction.ChangeMessageInput(message))
        }
    }

    fun onTriggerAutoComplete() {
        inputScope.launch {
            when (val firstItem = inputState.value.autoCompleteItems.firstOrNull()) {
                is AutoCompleteItem.Emote -> {
                    inputActions.emit(
                        InputAction.AppendEmote(emote = firstItem.emote, autocomplete = true),
                    )
                }

                is AutoCompleteItem.User -> {
                    inputActions.emit(
                        InputAction.AppendChatter(chatter = firstItem.chatter, autocomplete = true),
                    )
                }

                null -> {}
            }
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
            is Action.ChangeRecentEmotes -> reduce(state)
            is Action.ChangeRoomState -> reduce(state)
            is Action.ChangeUserState -> reduce(state)
            is Action.RemoveContent -> reduce(state)
            is Action.UpdatePoll -> reduce(state)
            is Action.UpdatePrediction -> reduce(state)
            is Action.UpdateStreamMetadata -> reduce(state)
            is Action.UpdateChatterPronouns -> reduce(state)
            is Action.AddRichEmbed -> reduce(state)
            is Action.LoadChat -> reduce(state)
            is Action.LoadEmotes -> reduce(state)
            is Action.LoadStreamDetails -> reduce(state)
            is Action.UpdateRaidAnnouncement -> reduce(state)
            is Action.UpdatePinnedMessage -> reduce(state)
        }
    }

    private suspend fun Action.LoadChat.reduce(state: State): State {
        if (state is State.Chatting && state.user.login == channelLogin) return state

        val prefs: AppPreferences = preferencesRepository.currentPreferences.first()
        val channelUser: User =
            twitchRepository.loadUsersByLogin(logins = listOf(channelLogin))
                ?.firstOrNull()
                ?: error("User not loaded")

        return State.Chatting(
            user = channelUser,
            appUser = prefs.appUser as AppUser.LoggedIn,
            chatters = persistentSetOf(
                Chatter(
                    id = channelUser.id,
                    login = channelUser.login,
                    displayName = channelUser.displayName,
                ),
            ),
            maxAdapterCount = AppPreferences.Defaults.ChatBufferLimit,
        )
    }

    @Suppress("UnusedReceiverParameter")
    private suspend fun Action.LoadStreamDetails.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            stream = twitchRepository.loadStreamWithUser(channelId = state.user.id),
        )
    }

    private suspend fun Action.LoadEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state

        return withContext(Dispatchers.IO) {
            val globalBadges: Deferred<PersistentList<TwitchBadge>?> =
                async {
                    try {
                        emotesRepository.loadGlobalBadges().toPersistentList()
                    } catch (e: Exception) {
                        logError<ChatViewModel>(e) { "Failed to load global badges" }
                        null
                    }
                }

            val channelBadges: Deferred<PersistentList<TwitchBadge>?> =
                async {
                    try {
                        emotesRepository.loadChannelBadges(channelId).toPersistentList()
                    } catch (e: Exception) {
                        logError<ChatViewModel>(e) { "Failed to load badges for channel $channelId" }
                        null
                    }
                }

            val cheerEmotes: PersistentMap<String, Emote>? =
                try {
                    twitchRepository.loadCheerEmotes(userId = channelId)
                        .associateBy { emote -> emote.name }
                        .toPersistentHashMap()
                } catch (e: Exception) {
                    logError<ChatViewModel>(e) { "Failed to load cheermotes for channel $channelId" }
                    null
                }

            val pickableEmotes: PersistentList<EmoteSetItem> =
                loadPickableEmotes(
                    channelId = channelId,
                    channelName = state.user.displayName,
                    emoteSets = state.userState.emoteSets,
                )

            state.copy(
                cheerEmotes = cheerEmotes ?: state.cheerEmotes,
                pickableEmotes = pickableEmotes,
                channelBadges = channelBadges.await() ?: state.channelBadges,
                globalBadges = globalBadges.await() ?: state.globalBadges,
            )
        }
    }

    private suspend fun loadPickableEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): PersistentList<EmoteSetItem> {
        return coroutineScope {
            emoteListSourcesProvider.getSources()
                .map { source ->
                    async {
                        try {
                            source.getEmotes(
                                channelId = channelId,
                                channelName = channelName,
                                emoteSets = emoteSets,
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
            messages.lastOrNull { message ->
                message.body != null && message.body?.chatter?.id == state.appUser.id
            }?.timestamp

        // Remember names of chatters
        val newChatters: PersistentSet<Chatter> =
            messages.asSequence()
                .mapNotNull { message -> message.body?.chatter }
                .toPersistentSet()

        val newMessages: PersistentList<ChatEvent> =
            state.chatMessages
                .addAll(messages)
                .distinct()
                .toPersistentList()

        // We alternate the background of each chat row.
        // If we remove just one item, the backgrounds will shift, so we always need to remove
        // an even number of items.
        val maxCount =
            state.maxAdapterCount.roundUpOddToEven() + if (newMessages.size.isOdd) 1 else 0

        return state.copy(
            chatMessages = newMessages
                .filterIsInstance<ChatEvent.Message>()
                .takeLast(maxCount)
                .toPersistentList(),
            lastSentMessageInstant = lastSentMessageInstant
                ?: state.lastSentMessageInstant,
            chatters = state.chatters.addAll(newChatters),
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
            emoteSets = userState.emoteSets,
        )

        return state.copy(
            userState = userState,
            pickableEmotes = pickableEmotes,
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
                slowModeDuration = delta.slowModeDuration ?: state.roomState.slowModeDuration,
            ),
        )
    }

    private fun Action.RemoveContent.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            removedContent = state.removedContent.add(removedContent),
        )
    }

    private fun Action.ChangeRecentEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(recentEmotes = recentEmotes)
    }

    private fun Action.UpdatePoll.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            ongoingEvents = state.ongoingEvents.copy(
                poll = poll,
            ),
        )
    }

    private fun Action.UpdatePrediction.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            ongoingEvents = state.ongoingEvents.copy(
                prediction = prediction,
            ),
        )
    }

    private fun Action.UpdateStreamMetadata.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            stream = state.stream?.copy(
                title = streamTitle ?: state.stream.title,
                gameName = gameName ?: state.stream.gameName,
                viewerCount = viewerCount ?: state.stream.viewerCount,
            ),
        )
    }

    private fun Action.AddRichEmbed.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            richEmbeds = state.richEmbeds.put(
                key = richEmbed.messageId,
                value = richEmbed,
            ),
        )
    }

    private fun Action.UpdateChatterPronouns.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            pronouns = state.pronouns.putAll(pronouns),
        )
    }

    private fun Action.UpdatePinnedMessage.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            ongoingEvents = state.ongoingEvents.copy(
                pinnedMessage = pinnedMessage,
            ),
        )
    }

    private fun Action.UpdateRaidAnnouncement.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            ongoingEvents = state.ongoingEvents.copy(
                outgoingRaid = raid,
            ),
        )
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
                inReplyToId = inputState.replyingTo?.body?.messageId,
            )

            val usedEmotes: List<RecentEmote> =
                inputState.inputMessage
                    .text
                    .split(' ')
                    .mapNotNull { word ->
                        state.allEmotesMap[word]?.let { emote ->
                            RecentEmote(
                                name = word,
                                url = emote.urls.getBestUrl(
                                    screenDensity = screenDensity,
                                    isDarkTheme = isDarkTheme,
                                ),
                                usedAt = currentTime,
                            )
                        }
                    }

            emotesRepository.insertRecentEmotes(usedEmotes)
        }

        return inputState.copy(
            inputMessage = TextFieldValue(""),
            replyingTo = null,
        )
    }

    private fun InputAction.ChangeMessageInput.reduce(inputState: InputState): InputState {
        return inputState.copy(inputMessage = message)
    }

    private fun InputAction.AppendEmote.reduce(inputState: InputState): InputState {
        return appendTextToInput(
            inputState = inputState,
            text = emote.name,
            replaceLastWord = autocomplete,
        )
    }

    private fun InputAction.AppendChatter.reduce(inputState: InputState): InputState {
        return appendTextToInput(
            inputState = inputState,
            text = "${ChatPrefixConstants.ChatterPrefix}${chatter.displayName}",
            replaceLastWord = autocomplete,
        )
    }

    private fun InputAction.ReplyToMessage.reduce(inputState: InputState): InputState {
        return inputState.copy(replyingTo = chatEvent)
    }

    private fun InputAction.UpdateAutoCompleteItems.reduce(inputState: InputState): InputState {
        return inputState.copy(autoCompleteItems = items)
    }

    private fun appendTextToInput(
        inputState: InputState,
        text: String,
        replaceLastWord: Boolean,
    ): InputState {
        val previousWord = inputState.inputMessage
            .getTextBeforeSelection(inputState.inputMessage.text.length)
            .takeLastWhile { it != ' ' }

        val textBefore = inputState.inputMessage
            .getTextBeforeSelection(inputState.inputMessage.text.length)
            .removeSuffix(
                if (replaceLastWord) previousWord else "",
            )

        val textAfter = inputState.inputMessage
            .getTextAfterSelection(inputState.inputMessage.text.length)

        return inputState.copy(
            inputMessage = inputState.inputMessage.copy(
                text = "${textBefore}$text $textAfter",
                selection = TextRange(
                    index = textBefore.length + text.length + 1,
                ),
            ),
        )
    }

    private suspend fun emitTestEvents() {
        actions.emit(
            Action.UpdatePrediction(
                prediction = Prediction(
                    id = "0c64f437-7481-46a3-9d80-2834cc415dfc",
                    title = "ANTOINE GAGNE ?",
                    status = Prediction.Status.Active,
                    createdAt = Instant.parse("2023-02-08T20:34:35.839478452Z"),
                    endedAt = null,
                    lockedAt = null,
                    outcomes = listOf(
                        Prediction.Outcome(
                            id = "1df7ac61-7912-4c82-89d3-d7781c0c182b",
                            title = "OUI",
                            color = "#1e69ff",
                            totalPoints = 50,
                            totalUsers = 0,
                            badge = Badge(id = "predictions", version = "blue-1"),
                        ),
                        Prediction.Outcome(
                            id = "a6225650-401b-4874-83aa-839f747d5f55",
                            title = "NON",
                            color = "#e0008e",
                            totalPoints = 100,
                            totalUsers = 0,
                            badge = Badge(id = "predictions", version = "pink-2"),
                        ),
                    ),
                    predictionWindow = 2.minutes,
                    winningOutcome = null,
                ),
            ),
        )

        actions.emit(
            Action.UpdatePoll(
                poll = Poll(
                    pollId = "0c64f437-7481-46a3-9d80-2834cc415dfc",
                    title = "ANTOINE GAGNE ?",
                    status = Poll.Status.Active,
                    startedAt = Instant.parse("2023-02-08T20:34:35.839478452Z"),
                    endedAt = null,
                    choices = listOf(
                        Poll.Choice(
                            choiceId = "1",
                            title = "Étoiles",
                            votes = Poll.Votes(
                                total = 12345,
                                bits = 123,
                                channelPoints = 50,
                                base = 1412,
                            ),
                            totalVoters = 1000,
                        ),
                        Poll.Choice(
                            choiceId = "1",
                            title = "AntoineDaniel",
                            votes = Poll.Votes(
                                total = 102345,
                                bits = 123,
                                channelPoints = 50,
                                base = 1412,
                            ),
                            totalVoters = 1000,
                        ),
                        Poll.Choice(
                            choiceId = "1",
                            title = "HortyUnderscore",
                            votes = Poll.Votes(
                                total = 52450,
                                bits = 123,
                                channelPoints = 50,
                                base = 1412,
                            ),
                            totalVoters = 1000,
                        ),
                    ),
                    duration = 3.minutes,
                    remainingDuration = 53.seconds,
                    totalVoters = 133143,
                    votes = Poll.Votes(
                        total = 134356,
                        bits = 1311,
                        channelPoints = 2345,
                        base = 757,
                    ),
                ),
            ),
        )
    }
}
