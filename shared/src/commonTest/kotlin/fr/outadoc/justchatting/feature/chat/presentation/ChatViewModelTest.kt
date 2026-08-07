package fr.outadoc.justchatting.feature.chat.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.AuthApi
import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSource
import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.emotes.domain.GetRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.InsertRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.RecentEmotesApi
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.LocalPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronouns
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.MessageNotSentException
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testClock =
        object : Clock {
            override fun now(): Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)
        }

    private val channelUser =
        User(
            id = "channel-id",
            login = "channelname",
            displayName = "ChannelName",
            description = "A streamer",
            profileImageUrl = "https://example.com/avatar.png",
            createdAt = Instant.fromEpochMilliseconds(1_000_000),
            usedAt = null,
        )

    private val otherChannelUser =
        User(
            id = "other-channel-id",
            login = "otherchannel",
            displayName = "OtherChannel",
            description = "Another streamer",
            profileImageUrl = "https://example.com/other-avatar.png",
            createdAt = Instant.fromEpochMilliseconds(2_000_000),
            usedAt = null,
        )

    private val channelChatter =
        Chatter(
            id = channelUser.id,
            login = channelUser.login,
            displayName = channelUser.displayName,
        )

    private val loggedInAppUser =
        AppUser.LoggedIn(
            userId = "app-user-id",
            userLogin = "appuser",
            token = "valid-token",
        )

    private val pickableEmote =
        Emote(
            name = "Kappa",
            urls = EmoteUrls("https://example.com/kappa.png"),
        )

    private lateinit var twitchRepository: FakeTwitchRepository
    private lateinit var chatRepository: FakeChatRepository
    private lateinit var recentEmotesApi: FakeRecentEmotesApi
    private lateinit var viewModel: ChatViewModel

    private val viewModelStores = mutableListOf<ViewModelStore>()

    private val dispatchersProvider = TestDispatchersProvider(testDispatcher)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        twitchRepository = FakeTwitchRepository()
        twitchRepository.users.value =
            mapOf(
                channelUser.id to channelUser,
                otherChannelUser.id to otherChannelUser,
            )

        chatRepository = FakeChatRepository()
        recentEmotesApi = FakeRecentEmotesApi()

        viewModel = createViewModel()
    }

    @AfterTest
    fun tearDown() {
        // Cancels viewModelScope, and with it every chat session pipeline, so that
        // no coroutine outlives the test and touches a reset dispatcher.
        viewModelStores.forEach { store -> store.clear() }
        testDispatcher.scheduler.advanceUntilIdle()

        Dispatchers.resetMain()
    }

    /**
     * Each call gets its own [ViewModelStore], so a test can build a second view model
     * with different collaborators instead of getting the cached one back.
     */
    private fun createViewModel(preferenceRepository: PreferenceRepository = FakePreferenceRepository()): ChatViewModel {
        val store = ViewModelStore()
        viewModelStores += store

        return ViewModelProvider.create(
            store = store,
            factory =
            viewModelFactory {
                initializer {
                    ChatViewModel(
                        clock = testClock,
                        twitchRepository = twitchRepository,
                        getRecentEmotes = GetRecentEmotesUseCase(recentEmotesApi),
                        chatRepository = chatRepository,
                        authRepository =
                        AuthRepository(
                            preferenceRepository = preferenceRepository,
                            authApi = FakeAuthApi(),
                            oAuthAppCredentials =
                            OAuthAppCredentials(
                                clientId = "client-id",
                                redirectUri = "https://example.com/callback",
                            ),
                            dispatchersProvider = dispatchersProvider,
                        ),
                        filterAutocompleteItemsUseCase = FilterAutocompleteItemsUseCase(),
                        pronounsRepository =
                        PronounsRepository(
                            pronounsApi = FakePronounsApi(),
                            localPronounsApi = FakeLocalPronounsApi(),
                            preferenceRepository = preferenceRepository,
                            dispatchersProvider = dispatchersProvider,
                        ),
                        createShortcutForChannel = NoopCreateShortcutForChannelUseCase(),
                        chatEventViewMapper = ChatEventViewMapper(),
                        loadEmotesAndBadges =
                        LoadEmotesAndBadgesUseCase(
                            twitchRepository = twitchRepository,
                            emoteListSourcesProvider =
                            EmoteListSourcesProvider {
                                listOf(
                                    FakeEmoteListSource(
                                        listOf(EmoteSetItem.Emote(pickableEmote)),
                                    ),
                                )
                            },
                        ),
                        submitMessage =
                        SubmitMessageUseCase(
                            clock = testClock,
                            twitchRepository = twitchRepository,
                            insertRecentEmotes = InsertRecentEmotesUseCase(recentEmotesApi),
                        ),
                        dispatchersProvider = dispatchersProvider,
                    )
                }
            },
        )[ChatViewModel::class]
    }

    private suspend fun awaitChatting(predicate: (ChatViewModel.State.Chatting) -> Boolean = { true }): ChatViewModel.State.Chatting = viewModel.state
        .filterIsInstance<ChatViewModel.State.Chatting>()
        .first(predicate)

    private suspend fun pushChatEvent(
        event: ChatEvent,
        channelId: String = channelUser.id,
    ) {
        val events = chatRepository.eventsFor(channelId)
        events.subscriptionCount.first { count -> count > 0 }
        events.emit(event)
    }

    private fun chatMessageEvent(
        id: String = "message-id",
        text: String = "hello world",
        userId: String = "chatter-id",
        userLogin: String = "chatter",
        userName: String = "Chatter",
        sourceRoomId: String? = null,
    ): ChatEvent.Message.ChatMessage = ChatEvent.Message.ChatMessage(
        timestamp = testClock.now(),
        id = id,
        userId = userId,
        userLogin = userLogin,
        userName = userName,
        message = text,
        color = null,
        isAction = false,
        embeddedEmotes = emptyList(),
        badges = null,
        isFirstMessageByUser = false,
        rewardId = null,
        inReplyTo = null,
        sourceRoomId = sourceRoomId,
    )

    @Test
    fun `initial state is Initial with an empty input`() {
        assertEquals(ChatViewModel.State.Initial, viewModel.state.value)
        assertEquals(ChatViewModel.InputState(), viewModel.inputState.value)
    }

    @Test
    fun `loadChat loads the channel and reaches Chatting`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)

        val state = awaitChatting()

        assertEquals(channelUser, state.user)
        assertEquals(loggedInAppUser, state.appUser)
        assertEquals(AppPreferences.Defaults.ChatBufferLimit, state.maxAdapterCount)
        assertTrue(channelChatter in state.chatters)
    }

    @Test
    fun `loadChat marks the channel as visited`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)

        awaitChatting()

        assertEquals(listOf(channelUser.id), twitchRepository.visitedChannels)
    }

    @Test
    fun `loadChat without a logged-in user fails`() = runTest(testDispatcher) {
        val loggedOutViewModel = createViewModel(FakePreferenceRepository(apiToken = null))

        loggedOutViewModel.loadChat(channelUser.id)
        advanceUntilIdle()

        assertIs<ChatViewModel.State.Failed>(loggedOutViewModel.state.value)
        assertTrue(chatRepository.eventFlowRequests.isEmpty())
    }

    @Test
    fun `a channel whose user cannot be loaded stays in the Loading state`() = runTest(testDispatcher) {
        twitchRepository.userError = IllegalStateException("user not found")

        viewModel.loadChat(channelUser.id)
        advanceUntilIdle()

        // The user pipeline only logs the failure, so nothing ever promotes the state
        // to Chatting and no chat connection is opened.
        assertIs<ChatViewModel.State.Loading>(viewModel.state.value)
        assertTrue(chatRepository.eventFlowRequests.isEmpty())
    }

    @Test
    fun `incoming chat messages are added to the state`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        pushChatEvent(
            chatMessageEvent(
                id = "message-1",
                text = "hello chat",
                userId = "chatter-1",
                userLogin = "chatter1",
                userName = "Chatter1",
            ),
        )

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.chatMessages.isNotEmpty()
            } as ChatViewModel.State.Chatting

        val message = state.chatMessages.single()
        assertEquals("hello chat", message.body?.message)

        val expectedChatter =
            Chatter(
                id = "chatter-1",
                login = "chatter1",
                displayName = "Chatter1",
            )
        assertEquals(expectedChatter, message.body?.chatter)
        assertTrue(expectedChatter in state.chatters)
    }

    @Test
    fun `connection status changes are reflected in the state`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        chatRepository.connectionStatusFor(channelUser.id).value =
            ConnectionStatus(
                isAlive = true,
                registeredListeners = 1,
            )

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.connectionStatus.isAlive
            } as ChatViewModel.State.Chatting

        assertEquals(
            ConnectionStatus(isAlive = true, registeredListeners = 1),
            state.connectionStatus,
        )
    }

    @Test
    fun `stream details are loaded into the state`() = runTest(testDispatcher) {
        val stream =
            Stream(
                id = "stream-id",
                userId = channelUser.id,
                category = null,
                title = "Stream title",
                viewerCount = 123,
                startedAt = Instant.fromEpochMilliseconds(1_600_000_000_000),
            )
        twitchRepository.streams.value = mapOf(channelUser.id to stream)

        viewModel.loadChat(channelUser.id)

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.stream != null
            } as ChatViewModel.State.Chatting

        assertEquals(stream, state.stream)
    }

    @Test
    fun `emotes and badges are loaded into the state`() = runTest(testDispatcher) {
        val globalBadge =
            TwitchBadge(
                setId = "subscriber",
                version = "0",
                urls = EmoteUrls("https://example.com/global-badge.png"),
            )
        val channelBadge =
            TwitchBadge(
                setId = "bits",
                version = "100",
                urls = EmoteUrls("https://example.com/channel-badge.png"),
            )
        val cheerEmote =
            Emote(
                name = "Cheer",
                urls = EmoteUrls("https://example.com/cheer.png"),
                bitsValue = 100,
            )
        twitchRepository.globalBadges = listOf(globalBadge)
        twitchRepository.channelBadges = listOf(channelBadge)
        twitchRepository.cheerEmotes = listOf(cheerEmote)

        viewModel.loadChat(channelUser.id)

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.pickableEmotes.isNotEmpty()
            } as ChatViewModel.State.Chatting

        assertEquals(listOf(EmoteSetItem.Emote(pickableEmote)), state.pickableEmotes.toList())
        assertEquals(listOf(globalBadge), state.globalBadges.toList())
        assertEquals(listOf(channelBadge), state.channelBadges.toList())
        assertEquals(cheerEmote, state.cheerEmotes["Cheer"])
    }

    @Test
    fun `recent emotes are filtered to emotes available in the channel`() = runTest(testDispatcher) {
        recentEmotesApi.recentEmotes.value =
            listOf(
                RecentEmote(
                    name = pickableEmote.name,
                    url = "https://example.com/kappa.png",
                    usedAt = testClock.now(),
                ),
                RecentEmote(
                    name = "EmoteFromAnotherChannel",
                    url = "https://example.com/other.png",
                    usedAt = testClock.now(),
                ),
            )

        viewModel.loadChat(channelUser.id)

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.recentEmotes.isNotEmpty()
            } as ChatViewModel.State.Chatting

        assertEquals(listOf(pickableEmote.name), state.recentEmotes.map { emote -> emote.name })
    }

    @Test
    fun `pronouns are fetched for chatters`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        advanceTimeBy(4.seconds)

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.pronouns.isNotEmpty()
            } as ChatViewModel.State.Chatting

        assertEquals("they", state.pronouns[channelChatter]?.nominative)
    }

    @Test
    fun `source channels are fetched for shared chat messages`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        pushChatEvent(
            chatMessageEvent(
                id = "shared-message",
                sourceRoomId = otherChannelUser.id,
            ),
        )

        advanceTimeBy(2.seconds)

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.sourceChannels.isNotEmpty()
            } as ChatViewModel.State.Chatting

        assertEquals(otherChannelUser, state.sourceChannels[otherChannelUser.id])
    }

    @Test
    fun `typing updates the input state`() {
        viewModel.onMessageInputChanged(
            message = "hello",
            selectionRange = 5..5,
        )

        assertEquals("hello", viewModel.inputState.value.message)
        assertEquals(5..5, viewModel.inputState.value.selectionRange)
    }

    @Test
    fun `appendEmote inserts the emote into the message input`() {
        viewModel.appendEmote(pickableEmote, autocomplete = false)

        assertEquals("Kappa ", viewModel.inputState.value.message)
    }

    @Test
    fun `submit sends the message and clears the input`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        viewModel.onMessageInputChanged(
            message = "hello chat",
            selectionRange = 10..10,
        )
        viewModel.submit(
            screenDensity = 2f,
            isDarkTheme = false,
        )

        val sent = twitchRepository.sentMessages.single()
        assertEquals(channelUser.id, sent.channelUserId)
        assertEquals("hello chat", sent.message)
        assertNull(sent.inReplyToMessageId)

        val input = viewModel.inputState.value
        assertEquals("", input.message)
        assertEquals("hello chat", input.lastSentMessage)
        assertTrue(input.canReuseLastMessage)
    }

    @Test
    fun `a message that fails to send is reported in the chat`() = runTest(testDispatcher) {
        twitchRepository.sendMessageError =
            MessageNotSentException(
                message = "message not sent",
                dropReasonCode = "channel_settings",
                dropReasonMessage = "Followers-only mode is enabled",
            )

        viewModel.loadChat(channelUser.id)
        awaitChatting()

        viewModel.onMessageInputChanged(
            message = "hello chat",
            selectionRange = 10..10,
        )
        viewModel.submit(
            screenDensity = 2f,
            isDarkTheme = false,
        )

        val state = awaitChatting { state -> state.chatMessages.isNotEmpty() }
        val error = assertIs<ChatListItem.Message.Highlighted>(state.chatMessages.single())

        assertEquals(
            "Followers-only mode is enabled",
            error.metadata.subtitle?.localizedString(),
        )
    }

    @Test
    fun `emotes used in a sent message are recorded as recent`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)

        // The emote has to be known to the channel before it can be recognised in the input.
        awaitChatting { state -> state.pickableEmotes.isNotEmpty() }

        viewModel.onMessageInputChanged(
            message = "hello ${pickableEmote.name}",
            selectionRange = 11..11,
        )
        viewModel.submit(
            screenDensity = 2f,
            isDarkTheme = false,
        )
        advanceUntilIdle()

        assertEquals(
            listOf(pickableEmote.name),
            recentEmotesApi.insertedEmotes.map { emote -> emote.name },
        )
    }

    @Test
    fun `submitting a reply passes the replied-to message id`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        val originalMessage =
            ChatListItem.Message.Simple(
                body =
                ChatListItem.Message.Body(
                    messageId = "original-message-id",
                    message = "original message",
                    chatter =
                    Chatter(
                        id = "chatter-1",
                        login = "chatter1",
                        displayName = "Chatter1",
                    ),
                ),
                timestamp = testClock.now(),
            )

        viewModel.onReplyToMessage(originalMessage)
        assertEquals(originalMessage, viewModel.inputState.value.replyingTo)

        viewModel.onMessageInputChanged(
            message = "a reply",
            selectionRange = 7..7,
        )
        viewModel.submit(
            screenDensity = 2f,
            isDarkTheme = false,
        )

        assertEquals(
            "original-message-id",
            twitchRepository.sentMessages.single().inReplyToMessageId,
        )
        assertNull(viewModel.inputState.value.replyingTo)
    }

    @Test
    fun `reuse last message restores the previous input`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        viewModel.onMessageInputChanged(
            message = "hello chat",
            selectionRange = 10..10,
        )
        viewModel.submit(
            screenDensity = 2f,
            isDarkTheme = false,
        )

        viewModel.onReuseLastMessageClicked()

        val input = viewModel.inputState.value
        assertEquals("hello chat", input.message)
        assertEquals(10..10, input.selectionRange)
    }

    @Test
    fun `autocomplete suggests matching chatters when typing a mention`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        viewModel.onMessageInputChanged(
            message = "@chan",
            selectionRange = 5..5,
        )

        val input =
            viewModel.inputState.first { inputState ->
                inputState.autoCompleteItems.isNotEmpty()
            }

        val item = input.autoCompleteItems.single()
        assertIs<AutoCompleteItem.User>(item)
        assertEquals(channelChatter, item.chatter)
    }

    @Test
    fun `switching channels resets the state and connects to the new channel`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        pushChatEvent(chatMessageEvent(text = "message in the first channel"))
        viewModel.state.first { state ->
            state is ChatViewModel.State.Chatting && state.chatMessages.isNotEmpty()
        }

        viewModel.loadChat(otherChannelUser.id)

        val state =
            viewModel.state.first { state ->
                state is ChatViewModel.State.Chatting && state.user.id == otherChannelUser.id
            } as ChatViewModel.State.Chatting

        assertEquals(otherChannelUser, state.user)
        assertTrue(state.chatMessages.isEmpty())
        assertEquals(
            otherChannelUser.id,
            chatRepository.eventFlowRequests.last().first.id,
        )
    }

    @Test
    fun `switching channels unsubscribes from the previous channel`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        pushChatEvent(chatMessageEvent(text = "message in the first channel"))
        awaitChatting { state -> state.chatMessages.isNotEmpty() }

        viewModel.loadChat(otherChannelUser.id)
        awaitChatting { state -> state.user.id == otherChannelUser.id }
        advanceUntilIdle()

        assertEquals(
            0,
            chatRepository.eventsFor(channelUser.id).subscriptionCount.value,
            "the abandoned channel should have no remaining subscribers",
        )
        assertEquals(
            1,
            chatRepository.eventsFor(otherChannelUser.id).subscriptionCount.value,
            "the new channel should be subscribed to exactly once",
        )
    }

    @Test
    fun `loading the same channel again keeps the existing state`() = runTest(testDispatcher) {
        viewModel.loadChat(channelUser.id)
        awaitChatting()

        pushChatEvent(chatMessageEvent(text = "a message"))
        viewModel.state.first { state ->
            state is ChatViewModel.State.Chatting && state.chatMessages.isNotEmpty()
        }

        viewModel.loadChat(channelUser.id)

        val state = assertIs<ChatViewModel.State.Chatting>(viewModel.state.value)
        assertEquals(channelUser, state.user)
        assertEquals(1, state.chatMessages.size)
        assertEquals(1, chatRepository.eventFlowRequests.size)
    }
}

private class FakeTwitchRepository : TwitchRepository {
    val users = MutableStateFlow<Map<String, User>>(emptyMap())
    val streams = MutableStateFlow<Map<String, Stream>>(emptyMap())

    var globalBadges: List<TwitchBadge> = emptyList()
    var channelBadges: List<TwitchBadge> = emptyList()
    var cheerEmotes: List<Emote> = emptyList()

    /** When set, [getUserById] emits this failure instead of reading from [users]. */
    var userError: Throwable? = null

    /** When set, [getStreamByUserId] emits this failure instead of reading from [streams]. */
    var streamError: Throwable? = null

    /** When set, [sendChatMessage] fails with this error after recording the attempt. */
    var sendMessageError: Throwable? = null

    val visitedChannels = mutableListOf<String>()
    val sentMessages = mutableListOf<SentMessage>()

    data class SentMessage(
        val channelUserId: String,
        val message: String,
        val inReplyToMessageId: String?,
    )

    override suspend fun getUserById(id: String): Flow<Result<User>> = userError
        ?.let { error -> flowOf(Result.failure(error)) }
        ?: users.mapNotNull { users -> users[id] }.map { user -> Result.success(user) }

    override suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>> = streamError
        ?.let { error -> flowOf(Result.failure(error)) }
        ?: streams.mapNotNull { streams -> streams[userId] }.map { stream -> Result.success(stream) }

    override suspend fun markChannelAsVisited(
        userId: String,
        visitedAt: Instant,
    ) {
        visitedChannels += userId
    }

    override suspend fun getGlobalBadges(): Result<List<TwitchBadge>> = Result.success(globalBadges)

    override suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>> = Result.success(channelBadges)

    override suspend fun getCheerEmotes(userId: String): Result<List<Emote>> = Result.success(cheerEmotes)

    override suspend fun sendChatMessage(
        channelUserId: String,
        message: String,
        inReplyToMessageId: String?,
        appUser: AppUser,
    ): Result<String> {
        sentMessages +=
            SentMessage(
                channelUserId = channelUserId,
                message = message,
                inReplyToMessageId = inReplyToMessageId,
            )
        return sendMessageError
            ?.let { error -> Result.failure(error) }
            ?: Result.success("sent-message-id")
    }

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> = error("Not used in tests")

    override suspend fun getFollowedChannels(): Flow<List<ChannelFollow>> = error("Not used in tests")

    override suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>> = users.map { users -> Result.success(ids.mapNotNull { id -> users[id] }) }

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>> = error("Not used in tests")

    override suspend fun getRecentChannels(): Flow<List<User>> = error("Not used in tests")

    override suspend fun forgetRecentChannel(userId: String) = error("Not used in tests")

    override suspend fun getFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
    ): Flow<FullSchedule> = error("Not used in tests")

    override suspend fun syncFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
        appUser: AppUser,
    ) = error("Not used in tests")

    override suspend fun syncFollowedStreams(appUser: AppUser) = error("Not used in tests")

    override suspend fun syncFollowedChannels(appUser: AppUser) = error("Not used in tests")
}

/**
 * Hands out a distinct flow per channel, so that tests can tell "the session for the
 * previous channel was torn down" apart from "the session is still listening".
 */
private class FakeChatRepository : ChatRepository {
    private val eventFlows = mutableMapOf<String, MutableSharedFlow<ChatEvent>>()
    private val connectionStatusFlows = mutableMapOf<String, MutableStateFlow<ConnectionStatus>>()

    val eventFlowRequests = mutableListOf<Pair<User, AppUser.LoggedIn>>()

    fun eventsFor(userId: String): MutableSharedFlow<ChatEvent> = eventFlows.getOrPut(userId) { MutableSharedFlow() }

    fun connectionStatusFor(userId: String): MutableStateFlow<ConnectionStatus> = connectionStatusFlows.getOrPut(userId) { MutableStateFlow(ConnectionStatus()) }

    override fun getChatEventFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> {
        eventFlowRequests += user to appUser
        return eventsFor(user.id)
    }

    override fun getConnectionStatusFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ConnectionStatus> = connectionStatusFor(user.id)
}

private class FakePreferenceRepository(
    apiToken: String? = "valid-token",
) : PreferenceRepository {
    private val preferences = MutableStateFlow(AppPreferences(apiToken = apiToken))

    override val currentPreferences: Flow<AppPreferences> = preferences

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        preferences.update(update)
    }
}

private class FakeAuthApi : AuthApi {
    override suspend fun validateToken(token: String): Result<AuthValidationResponse> = Result.success(
        AuthValidationResponse(
            clientId = "client-id",
            login = "appuser",
            userId = "app-user-id",
            scopes = persistentSetOf(
                "chat:read",
                "chat:edit",
                "user:read:follows",
                "user:write:chat",
            ),
        ),
    )

    override suspend fun revokeToken(
        clientId: String,
        token: String,
    ): Result<Unit> = Result.success(Unit)

    override fun getExternalAuthorizeUrl(
        oAuthAppCredentials: OAuthAppCredentials,
        scopes: Set<String>,
    ): Uri = Uri.parse("https://example.com/authorize")
}

private class FakeRecentEmotesApi : RecentEmotesApi {
    val recentEmotes = MutableStateFlow<List<RecentEmote>>(emptyList())
    val insertedEmotes = mutableListOf<RecentEmote>()

    override fun getAll(): Flow<List<RecentEmote>> = recentEmotes

    override fun insertAll(emotes: Collection<RecentEmote>) {
        insertedEmotes += emotes
    }
}

private class FakePronounsApi : PronounsApi {
    override suspend fun getPronouns(): Result<List<Pronoun>> = Result.success(emptyList())

    override suspend fun getUserPronouns(chatter: Chatter): Result<UserPronounIds> = Result.success(
        UserPronounIds(
            userId = chatter.id,
            mainPronounId = null,
            altPronounId = null,
        ),
    )
}

private class FakeLocalPronounsApi : LocalPronounsApi {
    private val theyThem =
        Pronoun(
            id = "they-them",
            nominative = "they",
            objective = "them",
            isSingular = true,
        )

    override suspend fun arePronounsSynced(): Boolean = true

    override suspend fun saveAndReplacePronouns(pronouns: List<Pronoun>) {}

    override suspend fun getPronounsForUser(userId: String): Flow<UserPronouns?> = flowOf(
        UserPronouns(
            userId = userId,
            mainPronoun = theyThem,
            altPronoun = null,
        ),
    )

    override suspend fun saveUserPronouns(userPronoun: UserPronounIds) {}
}

private class FakeEmoteListSource(
    private val emotes: List<EmoteSetItem>,
) : EmoteListSource<List<EmoteSetItem>> {
    override suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): Result<List<EmoteSetItem>> = Result.success(emotes)
}

private class TestDispatchersProvider(
    private val dispatcher: CoroutineDispatcher,
) : DispatchersProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
