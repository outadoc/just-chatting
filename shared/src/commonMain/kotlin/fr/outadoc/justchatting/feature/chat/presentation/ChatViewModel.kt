package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.domain.model.PinnedMessage
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.GetRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_header_recent
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal class ChatViewModel(
    private val clock: Clock,
    private val twitchRepository: TwitchRepository,
    private val getRecentEmotes: GetRecentEmotesUseCase,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val filterAutocompleteItemsUseCase: FilterAutocompleteItemsUseCase,
    private val pronounsRepository: PronounsRepository,
    private val createShortcutForChannel: CreateShortcutForChannelUseCase,
    private val chatEventViewMapper: ChatEventViewMapper,
    private val loadEmotesAndBadges: LoadEmotesAndBadgesUseCase,
    private val submitMessage: SubmitMessageUseCase,
) : ViewModel() {
    private val defaultScope = viewModelScope + CoroutineName("defaultScope")
    private val inputScope = viewModelScope + CoroutineName("inputScope")

    private val reducer = ChatStateReducer()

    sealed class Action {
        data class AddMessages(
            val messages: List<ChatListItem.Message>,
        ) : Action()

        data class ChangeRecentEmotes(
            val recentEmotes: List<Emote>,
        ) : Action()

        data class ChangeRoomState(
            val delta: ChatListItem.RoomStateDelta,
        ) : Action()

        data class ChangeConnectionStatus(
            val connectionStatus: ConnectionStatus,
        ) : Action()

        data class ChangeUserState(
            val userState: ChatListItem.UserState,
        ) : Action()

        data class RemoveContent(
            val removedContent: ChatListItem.RemoveContent,
        ) : Action()

        data class UpdatePoll(
            val poll: Poll,
        ) : Action()

        data class UpdatePrediction(
            val prediction: Prediction,
        ) : Action()

        data class UpdateRaidAnnouncement(
            val raid: Raid?,
        ) : Action()

        data class UpdatePinnedMessage(
            val pinnedMessage: PinnedMessage?,
        ) : Action()

        data class AddRichEmbed(
            val richEmbed: ChatListItem.RichEmbed,
        ) : Action()

        data class UpdateStreamMetadata(
            val viewerCount: Long? = null,
            val streamTitle: String? = null,
            val streamCategory: StreamCategory? = null,
        ) : Action()

        data class UpdateEmotes(
            val pickableEmotes: PersistentList<EmoteSetItem>,
            val globalBadges: PersistentList<TwitchBadge>?,
            val channelBadges: PersistentList<TwitchBadge>?,
            val cheerEmotes: PersistentMap<String, Emote>?,
        ) : Action()

        data class LoadChat(
            val userId: String,
            val appUser: AppUser.LoggedIn,
            val maxAdapterCount: Int,
        ) : Action()

        data class UpdateChatterPronouns(
            val pronouns: Map<Chatter, Pronoun?>,
        ) : Action()

        data class UpdateStreamDetails(
            val stream: Stream,
        ) : Action()

        data class ShowUserInfo(
            val userId: String?,
        ) : Action()

        data class UpdateStreamInfoVisibility(
            val isVisible: Boolean,
        ) : Action()

        data class UpdateUser(
            val user: User,
        ) : Action()
    }

    @Immutable
    sealed class State {
        data object Initial : State()

        data class Loading(
            val userId: String,
            val appUser: AppUser.LoggedIn,
            val maxAdapterCount: Int,
        ) : State()

        data class Failed(
            val throwable: Throwable,
        ) : State()

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
            val isStreamInfoVisible: Boolean = false,
        ) : State() {
            val allEmotesMap: ImmutableMap<String, Emote>
                get() =
                    pickableEmotes
                        .asSequence()
                        .filterIsInstance<EmoteSetItem.Emote>()
                        .map { item -> item.emote }
                        .distinctBy { emote -> emote.name }
                        .associateBy { emote -> emote.name }
                        .toImmutableMap()

            val pickableEmotesWithRecent: ImmutableList<EmoteSetItem>
                get() =
                    flatListOf(
                        EmoteSetItem.Header(
                            title = Res.string.chat_header_recent.desc(),
                            source = null,
                        ),
                        recentEmotes
                            .filter { recentEmote -> recentEmote.name in allEmotesMap }
                            .map { recentEmote -> EmoteSetItem.Emote(recentEmote) },
                    ).plus(pickableEmotes)
                        .toImmutableList()

            val messagePostConstraint: MessagePostConstraint?
                get() =
                    lastSentMessageInstant?.let {
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

        data object ReplaceInputWithLastSentMessage : InputAction()

        data class ReplyToMessage(
            val chatListItem: ChatListItem.Message? = null,
        ) : InputAction()

        data class UpdateAutoCompleteItems(
            val items: ImmutableList<AutoCompleteItem>,
        ) : InputAction()

        data class ClearAfterSubmit(
            val sentMessage: String,
        ) : InputAction()
    }

    @Immutable
    data class InputState(
        val message: String = "",
        val selectionRange: IntRange = 0..0,
        val replyingTo: ChatListItem.Message? = null,
        val lastSentMessage: String? = null,
        val autoCompleteItems: ImmutableList<AutoCompleteItem> = persistentListOf(),
    ) {
        val canReuseLastMessage: Boolean
            get() = message.isBlank() && lastSentMessage.isNullOrBlank().not()
    }

    private val actions =
        MutableSharedFlow<Action>(
            extraBufferCapacity = 16,
            replay = 1,
        )

    @OptIn(FlowPreview::class)
    val state: StateFlow<State> =
        actions
            .runningFold(State.Initial) { state: State, action -> reducer.reduce(action, state) }
            .debounce(100.milliseconds)
            .stateIn(
                scope = defaultScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = State.Initial,
            )

    private val inputActions = MutableSharedFlow<InputAction>()
    val inputState: StateFlow<InputState> =
        inputActions
            .runningFold(InputState()) { state: InputState, action -> reducer.reduce(action, state) }
            .stateIn(
                scope = inputScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = InputState(),
            )

    init {
        state
            .filterIsInstance<State.Chatting>()
            .map { state -> Triple(state.user.id, state.user.displayName, state.userState.emoteSets) }
            .distinctUntilChanged()
            .onEach { (channelId, channelName, emoteSets) ->
                defaultScope.launch {
                    withContext(DispatchersProvider.io) {
                        val action = loadEmotesAndBadges(
                            channelId = channelId,
                            channelName = channelName,
                            emoteSets = emoteSets,
                        )
                        actions.emit(action)
                    }
                }
            }.launchIn(defaultScope)

        state
            .mapNotNull { state ->
                when (state) {
                    is State.Chatting -> state.user.id
                    is State.Loading -> state.userId
                    else -> null
                }
            }.distinctUntilChanged()
            .onEach { userId ->
                twitchRepository.markChannelAsVisited(
                    userId = userId,
                    visitedAt = clock.now(),
                )
            }.flatMapLatest { userId ->
                twitchRepository.getUserById(userId)
            }.onEach { result ->
                result
                    .onSuccess { user ->
                        createShortcutForChannel(user)
                        actions.emit(Action.UpdateUser(user))
                    }.onFailure { exception ->
                        logError<ChatViewModel>(exception) { "Failed to load user" }
                    }
            }.launchIn(defaultScope)

        state
            .filterIsInstance<State.Chatting>()
            .mapNotNull { state -> state.user.id }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                twitchRepository.getStreamByUserId(userId = userId)
            }.onEach { result ->
                result
                    .onSuccess { stream ->
                        actions.emit(Action.UpdateStreamDetails(stream))
                    }.onFailure { exception ->
                        logError<ChatViewModel>(exception) { "Failed to load stream details for user" }
                    }
            }.launchIn(defaultScope)

        state
            .filterIsInstance<State.Chatting>()
            .map { state -> Pair(state.user, state.appUser) }
            .distinctUntilChanged()
            .flatMapLatest { (user, appUser) ->
                merge(
                    chatRepository
                        .getChatEventFlow(user, appUser)
                        .flatMapConcat { event ->
                            chatEventViewMapper.map(event).asFlow()
                        }.mapNotNull { event ->
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
                        },
                    chatRepository
                        .getConnectionStatusFlow(user, appUser)
                        .map { status -> Action.ChangeConnectionStatus(status) },
                )
            }.onEach { action -> actions.emit(action) }
            .launchIn(defaultScope)

        state
            .filterIsInstance<State.Chatting>()
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
            }.distinctUntilChanged()
            .onEach { (recentEmotes, allEmotesMap) ->
                val action =
                    Action.ChangeRecentEmotes(
                        recentEmotes =
                        recentEmotes
                            .filter { recentEmote -> recentEmote.name in allEmotesMap }
                            .map { recentEmote ->
                                Emote(
                                    name = recentEmote.name,
                                    urls = EmoteUrls(recentEmote.url),
                                )
                            },
                    )

                actions.emit(action)
            }.launchIn(defaultScope)

        state
            .filterIsInstance<State.Chatting>()
            .map { state -> state.chatters - state.pronouns.keys }
            .distinctUntilChanged()
            .debounce(3.seconds)
            .map { chatters -> pronounsRepository.fillPronounsFor(chatters) }
            .onEach { pronouns -> actions.emit(Action.UpdateChatterPronouns(pronouns)) }
            .launchIn(defaultScope)

        state
            .filterIsInstance<State.Chatting>()
            .distinctUntilChanged()
            .map { state ->
                Triple(
                    state.allEmotesMap,
                    state.chatters,
                    state.recentEmotes,
                )
            }.distinctUntilChanged()
            .flatMapLatest { (allEmotesMap, chatters, recentEmotes) ->
                inputState
                    .debounce(300.milliseconds)
                    .map { inputState ->
                        inputState.message
                            .substring(
                                startIndex = 0,
                                endIndex = inputState.selectionRange.first,
                            ).takeLastWhile { it != ' ' }
                    }.mapLatest { word ->
                        filterAutocompleteItemsUseCase(
                            filter = word,
                            allEmotesMap = allEmotesMap,
                            recentEmotes = recentEmotes,
                            chatters = chatters,
                        )
                    }.flowOn(DispatchersProvider.default)
            }.onEach { autoCompleteItems ->
                inputActions.emit(
                    InputAction.UpdateAutoCompleteItems(autoCompleteItems),
                )
            }.launchIn(viewModelScope)
    }

    fun loadChat(userId: String) {
        defaultScope.launch {
            val appUser = authRepository.currentUser.first() as AppUser.LoggedIn
            actions.emit(
                Action.LoadChat(
                    userId = userId,
                    appUser = appUser,
                    maxAdapterCount = AppPreferences.Defaults.ChatBufferLimit,
                ),
            )
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

    fun onShowStreamInfo() {
        defaultScope.launch {
            actions.emit(Action.UpdateStreamInfoVisibility(isVisible = true))
        }
    }

    fun onDismissStreamInfo() {
        defaultScope.launch {
            actions.emit(Action.UpdateStreamInfoVisibility(isVisible = false))
        }
    }

    fun onReplyToMessage(entry: ChatListItem.Message?) {
        inputScope.launch {
            inputActions.emit(InputAction.ReplyToMessage(entry))
        }
    }

    fun onMessageInputChanged(
        message: String,
        selectionRange: IntRange,
    ) {
        inputScope.launch {
            inputActions.emit(
                InputAction.ChangeMessageInput(
                    message = message,
                    selectionRange = selectionRange,
                ),
            )
        }
    }

    fun onReuseLastMessageClicked() {
        inputScope.launch {
            inputActions.emit(InputAction.ReplaceInputWithLastSentMessage)
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

    fun appendEmote(
        emote: Emote,
        autocomplete: Boolean,
    ) {
        inputScope.launch {
            inputActions.emit(InputAction.AppendEmote(emote, autocomplete))
        }
    }

    fun appendChatter(
        chatter: Chatter,
        autocomplete: Boolean,
    ) {
        inputScope.launch {
            inputActions.emit(InputAction.AppendChatter(chatter, autocomplete))
        }
    }

    fun submit(
        screenDensity: Float,
        isDarkTheme: Boolean,
    ) {
        val currentInputState = inputState.value
        if (currentInputState.message.isEmpty()) return

        val chatState = state.value as? State.Chatting ?: return

        inputScope.launch {
            inputActions.emit(InputAction.ClearAfterSubmit(sentMessage = currentInputState.message))
        }

        defaultScope.launch {
            val errorAction = submitMessage(
                channelUserId = chatState.user.id,
                message = currentInputState.message,
                inReplyToMessageId = currentInputState.replyingTo?.body?.messageId,
                appUser = chatState.appUser,
                allEmotesMap = chatState.allEmotesMap,
                screenDensity = screenDensity,
                isDarkTheme = isDarkTheme,
            )

            if (errorAction != null) {
                actions.emit(errorAction)
            }
        }
    }
}
