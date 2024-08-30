package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import dev.icerock.moko.resources.desc.desc
import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.domain.model.PinnedMessage
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.emotes.domain.GetRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.InsertRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.core.isOdd
import fr.outadoc.justchatting.utils.core.roundUpOddToEven
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.presentation.ViewModel
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal class ChatViewModel(
    private val clock: Clock,
    private val twitchRepository: TwitchRepository,
    private val getRecentEmotes: GetRecentEmotesUseCase,
    private val insertRecentEmotes: InsertRecentEmotesUseCase,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val emoteListSourcesProvider: EmoteListSourcesProvider,
    private val filterAutocompleteItemsUseCase: FilterAutocompleteItemsUseCase,
    private val pronounsRepository: PronounsRepository,
    private val createShortcutForChannel: CreateShortcutForChannelUseCase,
    private val chatEventViewMapper: ChatEventViewMapper,
) : ViewModel() {

    private val defaultScope = viewModelScope + CoroutineName("defaultScope")
    private val inputScope = viewModelScope + CoroutineName("inputScope")

    sealed class Action {
        data class AddMessages(val messages: List<ChatListItem.Message>) : Action()
        data class ChangeRecentEmotes(val recentEmotes: List<Emote>) : Action()
        data class ChangeRoomState(val delta: ChatListItem.RoomStateDelta) : Action()
        data class ChangeConnectionStatus(val connectionStatus: ConnectionStatus) : Action()
        data class ChangeUserState(val userState: ChatListItem.UserState) : Action()
        data class RemoveContent(val removedContent: ChatListItem.RemoveContent) : Action()
        data class UpdatePoll(val poll: Poll) : Action()
        data class UpdatePrediction(val prediction: Prediction) : Action()
        data class UpdateRaidAnnouncement(val raid: Raid?) : Action()
        data class UpdatePinnedMessage(val pinnedMessage: PinnedMessage?) : Action()
        data class AddRichEmbed(val richEmbed: ChatListItem.RichEmbed) : Action()
        data class UpdateStreamMetadata(
            val viewerCount: Long? = null,
            val streamTitle: String? = null,
            val streamCategory: StreamCategory? = null,
        ) : Action()

        data class LoadEmotes(val channelId: String) : Action()
        data class LoadChat(val userId: String) : Action()
        data class UpdateChatterPronouns(val pronouns: Map<Chatter, Pronoun?>) : Action()
        data class UpdateStreamDetails(val stream: Stream) : Action()
        data class ShowUserInfo(val userId: String?) : Action()
        data class UpdateUser(val user: User) : Action()
    }

    @Immutable
    sealed class State {
        data object Initial : State()

        data class Loading(
            val userId: String,
            val appUser: AppUser.LoggedIn,
            val maxAdapterCount: Int,
        ) : State()

        data class Failed(val throwable: Throwable) : State()

        data class Chatting(
            val user: User,
            val appUser: AppUser.LoggedIn,
            val stream: Stream? = null,
            val channelBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val chatMessages: PersistentList<ChatListItem.Message> = persistentListOf(),
            val chatters: PersistentSet<Chatter> = persistentHashSetOf(),
            val pronouns: PersistentMap<Chatter, Pronoun?> = persistentMapOf(),
            val cheerEmotes: PersistentMap<String, Emote> = persistentMapOf(),
            val globalBadges: PersistentList<TwitchBadge> = persistentListOf(),
            val lastSentMessageInstant: Instant? = null,
            val pickableEmotes: ImmutableList<EmoteSetItem> = persistentListOf(),
            val richEmbeds: PersistentMap<String, ChatListItem.RichEmbed> = persistentMapOf(),
            val recentEmotes: List<Emote> = emptyList(),
            val userState: ChatListItem.UserState = ChatListItem.UserState(),
            val roomState: RoomState = RoomState(),
            val ongoingEvents: OngoingEvents = OngoingEvents(),
            val removedContent: PersistentList<ChatListItem.RemoveContent> = persistentListOf(),
            val connectionStatus: ConnectionStatus = ConnectionStatus(),
            val maxAdapterCount: Int,
            val showInfoForUserId: String? = null,
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
                        title = MR.strings.chat_header_recent.desc(),
                        source = null,
                    ),
                    recentEmotes
                        .filter { recentEmote -> recentEmote.name in allEmotesMap }
                        .map { recentEmote -> EmoteSetItem.Emote(recentEmote) },
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
        data class AppendChatter(
            val chatter: Chatter,
            val autocomplete: Boolean,
        ) : InputAction()

        data class AppendEmote(
            val emote: Emote,
            val autocomplete: Boolean,
        ) : InputAction()

        data class ChangeMessageInput(
            val message: String,
            val selectionRange: IntRange,
        ) : InputAction()

        data class ReplyToMessage(val chatListItem: ChatListItem.Message? = null) : InputAction()

        data class UpdateAutoCompleteItems(val items: List<AutoCompleteItem>) : InputAction()

        data class Submit(
            val screenDensity: Float,
            val isDarkTheme: Boolean,
        ) : InputAction()
    }

    @Immutable
    data class InputState(
        val message: String = "",
        val selectionRange: IntRange = 0..0,
        val replyingTo: ChatListItem.Message? = null,
        val autoCompleteItems: List<AutoCompleteItem> = emptyList(),
    )

    private val actions = MutableSharedFlow<Action>(
        extraBufferCapacity = 16,
        replay = 1,
    )

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
            .mapNotNull { state -> state.user }
            .distinctUntilChanged()
            .onEach { user ->
                actions.emit(Action.LoadEmotes(user.id))
            }
            .launchIn(defaultScope)

        state
            .mapNotNull { state ->
                when (state) {
                    is State.Chatting -> state.user.id
                    is State.Loading -> state.userId
                    else -> null
                }
            }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                twitchRepository.getUserById(userId)
            }
            .onEach { result ->
                result
                    .onSuccess { user ->
                        actions.emit(Action.UpdateUser(user))

                        twitchRepository.markChannelAsVisited(
                            channel = user,
                            visitedAt = clock.now(),
                        )

                        createShortcutForChannel(user)
                    }
                    .onFailure { exception ->
                        logError<ChatViewModel>(exception) { "Failed to load user" }
                    }
            }
            .launchIn(defaultScope)

        state.filterIsInstance<State.Chatting>()
            .mapNotNull { state -> state.user.id }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                twitchRepository.getStreamByUserId(userId = userId)
            }
            .onEach { result ->
                result
                    .onSuccess { stream ->
                        actions.emit(Action.UpdateStreamDetails(stream))
                    }
                    .onFailure { exception ->
                        logError<ChatViewModel>(exception) { "Failed to load stream details for user" }
                    }
            }
            .launchIn(defaultScope)

        state.filterIsInstance<State.Chatting>()
            .mapNotNull { state -> state.user }
            .distinctUntilChanged()
            .onEach { user ->
                chatRepository
                    .getChatEventFlow(
                        channelId = user.id,
                        channelLogin = user.login,
                    )
                    .flatMapConcat { event ->
                        chatEventViewMapper.map(event).asFlow()
                    }
                    .mapNotNull { event ->
                        when (event) {
                            is ChatListItem.Message -> {
                                Action.AddMessages(listOf(event))
                            }

                            is ChatListItem.RoomStateDelta -> {
                                Action.ChangeRoomState(event)
                            }

                            is ChatListItem.UserState -> {
                                Action.ChangeUserState(event)
                            }

                            is ChatListItem.RemoveContent -> {
                                Action.RemoveContent(event)
                            }

                            is ChatListItem.PollUpdate -> {
                                Action.UpdatePoll(event.poll)
                            }

                            is ChatListItem.PredictionUpdate -> {
                                Action.UpdatePrediction(event.prediction)
                            }

                            is ChatListItem.BroadcastSettingsUpdate -> {
                                Action.UpdateStreamMetadata(
                                    streamTitle = event.streamTitle,
                                    streamCategory = event.streamCategory,
                                )
                            }

                            is ChatListItem.ViewerCountUpdate -> {
                                Action.UpdateStreamMetadata(
                                    viewerCount = event.viewerCount,
                                )
                            }

                            is ChatListItem.RichEmbed -> {
                                Action.AddRichEmbed(event)
                            }

                            is ChatListItem.RaidUpdate -> {
                                Action.UpdateRaidAnnouncement(
                                    raid = event.raid,
                                )
                            }

                            is ChatListItem.PinnedMessageUpdate -> {
                                Action.UpdatePinnedMessage(
                                    pinnedMessage = event.pinnedMessage,
                                )
                            }
                        }
                    }
                    .onEach { action -> actions.emit(action) }
                    .launchIn(defaultScope)

                chatRepository.getConnectionStatusFlow(user.id, user.login)
                    .map { status -> Action.ChangeConnectionStatus(status) }
                    .onEach { action -> actions.emit(action) }
                    .launchIn(defaultScope)
            }
            .launchIn(defaultScope)

        state.filterIsInstance<State.Chatting>()
            .map { state -> state.allEmotesMap }
            .distinctUntilChanged()
            .flatMapLatest { allEmotesMap ->
                getRecentEmotes()
                    .map { recentEmotes ->
                        Pair(
                            recentEmotes,
                            allEmotesMap,
                        )
                    }
            }
            .distinctUntilChanged()
            .onEach { (recentEmotes, allEmotesMap) ->
                val action = Action.ChangeRecentEmotes(
                    recentEmotes = recentEmotes
                        .filter { recentEmote -> recentEmote.name in allEmotesMap }
                        .map { recentEmote ->
                            Emote(
                                name = recentEmote.name,
                                urls = EmoteUrls(recentEmote.url),
                            )
                        },
                )

                actions.emit(action)
            }
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
            .map { state ->
                Triple(
                    state.allEmotesMap,
                    state.chatters,
                    state.recentEmotes,
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { (allEmotesMap, chatters, recentEmotes) ->
                inputState
                    .debounce(300.milliseconds)
                    .map { inputState ->
                        inputState.message.substring(
                            startIndex = 0,
                            endIndex = inputState.selectionRange.first,
                        )
                            .takeLastWhile { it != ' ' }
                    }
                    .mapLatest { word ->
                        filterAutocompleteItemsUseCase(
                            filter = word,
                            allEmotesMap = allEmotesMap,
                            recentEmotes = recentEmotes,
                            chatters = chatters,
                        )
                    }
                    .flowOn(DispatchersProvider.default)
            }
            .onEach { autoCompleteItems ->
                inputActions.emit(
                    InputAction.UpdateAutoCompleteItems(autoCompleteItems),
                )
            }
            .launchIn(viewModelScope)
    }

    fun loadChat(userId: String) {
        defaultScope.launch {
            actions.emit(Action.LoadChat(userId))
        }
    }

    fun onShowUserInfo(userId: String) {
        defaultScope.launch {
            actions.emit(Action.ShowUserInfo(userId = userId))
        }
    }

    fun onDismissUserInfo() {
        defaultScope.launch {
            actions.emit(Action.ShowUserInfo(userId = null))
        }
    }

    fun onReplyToMessage(entry: ChatListItem.Message?) {
        inputScope.launch {
            inputActions.emit(InputAction.ReplyToMessage(entry))
        }
    }

    fun onMessageInputChanged(message: String, selectionRange: IntRange) {
        inputScope.launch {
            inputActions.emit(
                InputAction.ChangeMessageInput(
                    message = message,
                    selectionRange = selectionRange,
                ),
            )
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
            is Action.UpdateStreamDetails -> reduce(state)
            is Action.UpdateRaidAnnouncement -> reduce(state)
            is Action.UpdatePinnedMessage -> reduce(state)
            is Action.ShowUserInfo -> reduce(state)
            is Action.UpdateUser -> reduce(state)
        }
    }

    private suspend fun Action.LoadChat.reduce(state: State): State {
        if (state is State.Chatting && state.user.id == userId) return state

        val appUser = authRepository.currentUser.first()

        return State.Loading(
            userId = userId,
            appUser = appUser as AppUser.LoggedIn,
            maxAdapterCount = AppPreferences.Defaults.ChatBufferLimit,
        )
    }

    private fun Action.UpdateStreamDetails.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(stream = stream)
    }

    private suspend fun Action.LoadEmotes.reduce(state: State): State {
        if (state !is State.Chatting) return state

        return withContext(DispatchersProvider.io) {
            val globalBadges: Deferred<PersistentList<TwitchBadge>?> =
                async {
                    twitchRepository.getGlobalBadges()
                        .fold(
                            onSuccess = { badges -> badges.toPersistentList() },
                            onFailure = { exception ->
                                logError<ChatViewModel>(exception) { "Failed to load global badges" }
                                null
                            },
                        )
                }

            val channelBadges: Deferred<PersistentList<TwitchBadge>?> =
                async {
                    twitchRepository.getChannelBadges(channelId)
                        .fold(
                            onSuccess = { badges -> badges.toPersistentList() },
                            onFailure = { exception ->
                                logError<ChatViewModel>(exception) { "Failed to load badges for channel $channelId" }
                                null
                            },
                        )
                }

            val cheerEmotes: PersistentMap<String, Emote>? =
                twitchRepository
                    .getCheerEmotes(userId = channelId)
                    .fold(
                        onSuccess = { emotes ->
                            emotes
                                .associateBy { emote -> emote.name }
                                .toPersistentHashMap()
                        },
                        onFailure = { exception ->
                            logError<ChatViewModel>(exception) { "Failed to load cheer emotes for channel $channelId" }
                            null
                        },
                    )

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
                        source
                            .getEmotes(
                                channelId = channelId,
                                channelName = channelName,
                                emoteSets = emoteSets,
                            )
                            .fold(
                                onSuccess = { emotes -> emotes },
                                onFailure = { exception ->
                                    logError<ChatViewModel>(exception) { "Failed to load emotes from source $source" }
                                    emptyList()
                                },
                            )
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
                message.body != null && message.body?.chatter?.id == state.appUser.userId
            }?.timestamp

        // Remember names of chatters
        val newChatters: PersistentSet<Chatter> =
            messages.asSequence()
                .mapNotNull { message -> message.body?.chatter }
                .toPersistentSet()

        val newMessages: PersistentList<ChatListItem> =
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
                .filterIsInstance<ChatListItem.Message>()
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

        val pickableEmotes: PersistentList<EmoteSetItem> =
            loadPickableEmotes(
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
                category = streamCategory ?: state.stream.category,
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

        if (pinnedMessage == null) {
            return state.copy(
                ongoingEvents = state.ongoingEvents.copy(
                    pinnedMessage = null,
                ),
            )
        }

        val matchingMessage: ChatListItem.Message =
            state.chatMessages.findLast { message ->
                message.body?.messageId == pinnedMessage.message.messageId
            } ?: return state.copy(
                ongoingEvents = state.ongoingEvents.copy(
                    pinnedMessage = null,
                ),
            )

        return state.copy(
            ongoingEvents = state.ongoingEvents.copy(
                pinnedMessage = OngoingEvents.PinnedMessage(
                    message = matchingMessage,
                    endsAt = pinnedMessage.message.endsAt,
                ),
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

    private fun Action.ShowUserInfo.reduce(state: State): State {
        if (state !is State.Chatting) return state
        return state.copy(
            showInfoForUserId = userId,
        )
    }

    private fun Action.UpdateUser.reduce(state: State): State {
        return when (state) {
            is State.Initial,
            is State.Failed,
            -> {
                state
            }

            is State.Loading -> {
                State.Chatting(
                    user = user,
                    appUser = state.appUser,
                    maxAdapterCount = state.maxAdapterCount,
                    chatters = persistentSetOf(
                        Chatter(
                            id = user.id,
                            login = user.login,
                            displayName = user.displayName,
                        ),
                    ),
                )
            }

            is State.Chatting -> {
                state.copy(
                    user = user,
                    chatters = state.chatters.add(
                        Chatter(
                            id = user.id,
                            login = user.login,
                            displayName = user.displayName,
                        ),
                    ),
                )
            }
        }
    }

    private fun InputAction.reduce(state: InputState): InputState {
        return when (this) {
            is InputAction.AppendChatter -> reduce(state)
            is InputAction.AppendEmote -> reduce(state)
            is InputAction.ChangeMessageInput -> reduce(state)
            is InputAction.ReplyToMessage -> reduce(state)
            is InputAction.Submit -> reduce(state)
            is InputAction.UpdateAutoCompleteItems -> reduce(state)
        }
    }

    private fun InputAction.Submit.reduce(inputState: InputState): InputState {
        if (inputState.message.isEmpty()) return inputState
        val state = state.value as? State.Chatting ?: return inputState

        defaultScope.launch {
            val currentTime = clock.now()

            twitchRepository
                .sendChatMessage(
                    channelUserId = state.user.id,
                    message = inputState.message,
                    inReplyToMessageId = inputState.replyingTo?.body?.messageId,
                )
                .onFailure { _ ->
                    actions.emit(
                        Action.AddMessages(
                            listOf(
                                ChatListItem.Message.Highlighted(
                                    timestamp = clock.now(),
                                    metadata = ChatListItem.Message.Highlighted.Metadata(
                                        title = MR.strings.chat_send_msg_error.desc(),
                                        subtitle = null,
                                    ),
                                    body = null,
                                ),
                            ),
                        ),
                    )
                }

            val usedEmotes: List<RecentEmote> =
                inputState.message
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

            insertRecentEmotes(usedEmotes)
        }

        return inputState.copy(
            message = "",
            selectionRange = 0..0,
            replyingTo = null,
        )
    }

    private fun InputAction.ChangeMessageInput.reduce(inputState: InputState): InputState {
        return inputState.copy(
            message = message,
            selectionRange = selectionRange,
        )
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
        return inputState.copy(replyingTo = chatListItem)
    }

    private fun InputAction.UpdateAutoCompleteItems.reduce(inputState: InputState): InputState {
        return inputState.copy(autoCompleteItems = items)
    }

    private fun appendTextToInput(
        inputState: InputState,
        text: String,
        replaceLastWord: Boolean,
    ): InputState {
        val previousWord = inputState.message
            .substring(startIndex = 0, endIndex = inputState.selectionRange.first)
            .takeLastWhile { it != ' ' }

        val textBefore = inputState.message
            .substring(startIndex = 0, endIndex = inputState.selectionRange.first)
            .removeSuffix(
                if (replaceLastWord) previousWord else "",
            )

        val textAfter = inputState.message
            .substring(inputState.selectionRange.last)

        return inputState.copy(
            message = "${textBefore}$text $textAfter",
            selectionRange = IntRange(
                start = textBefore.length + text.length + 1,
                endInclusive = textBefore.length + text.length + 1,
            ),
        )
    }

    private suspend fun emitTestEvents() {
        actions.emitAll(TestEvents.events)
    }
}
