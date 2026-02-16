package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.domain.model.PinnedMessage
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class ChatStateReducerTest {

    private val reducer = ChatStateReducer()

    private val testAppUser = AppUser.LoggedIn(
        userId = "app-user-id",
        userLogin = "appuser",
        token = "token123",
    )

    private val testUser = User(
        id = "user-123",
        login = "testuser",
        displayName = "TestUser",
        description = "A test user",
        profileImageUrl = "https://example.com/avatar.png",
        createdAt = Instant.fromEpochMilliseconds(1000000),
        usedAt = null,
    )

    private val testChattingState = ChatViewModel.State.Chatting(
        user = testUser,
        appUser = testAppUser,
        maxAdapterCount = 100,
    )

    private fun createMessage(
        messageId: String,
        text: String = "hello",
        chatterId: String = "chatter-1",
        chatterLogin: String = "chatter1",
        chatterDisplayName: String = "Chatter1",
        timestamp: Instant = Instant.fromEpochMilliseconds(1000),
    ): ChatListItem.Message.Simple = ChatListItem.Message.Simple(
        body = ChatListItem.Message.Body(
            messageId = messageId,
            message = text,
            chatter = Chatter(
                id = chatterId,
                login = chatterLogin,
                displayName = chatterDisplayName,
            ),
        ),
        timestamp = timestamp,
    )

    // region Action: LoadChat

    @Test
    fun `LoadChat from Initial returns Loading`() {
        val action = ChatViewModel.Action.LoadChat(
            userId = "user-123",
            appUser = testAppUser,
            maxAdapterCount = 100,
        )

        val result = reducer.reduce(action, ChatViewModel.State.Initial)

        assertIs<ChatViewModel.State.Loading>(result)
        assertEquals("user-123", result.userId)
        assertEquals(testAppUser, result.appUser)
        assertEquals(100, result.maxAdapterCount)
    }

    @Test
    fun `LoadChat from Chatting with same userId is a no-op`() {
        val action = ChatViewModel.Action.LoadChat(
            userId = "user-123",
            appUser = testAppUser,
            maxAdapterCount = 100,
        )

        val result = reducer.reduce(action, testChattingState)

        assertSame(testChattingState, result)
    }

    @Test
    fun `LoadChat from Chatting with different userId returns Loading`() {
        val action = ChatViewModel.Action.LoadChat(
            userId = "other-user",
            appUser = testAppUser,
            maxAdapterCount = 50,
        )

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Loading>(result)
        assertEquals("other-user", result.userId)
    }

    // endregion

    // region Action: UpdateUser

    @Test
    fun `UpdateUser from Loading transitions to Chatting`() {
        val loadingState = ChatViewModel.State.Loading(
            userId = "user-123",
            appUser = testAppUser,
            maxAdapterCount = 100,
        )
        val action = ChatViewModel.Action.UpdateUser(user = testUser)

        val result = reducer.reduce(action, loadingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(testUser, result.user)
        assertEquals(testAppUser, result.appUser)
        assertEquals(100, result.maxAdapterCount)
    }

    @Test
    fun `UpdateUser from Chatting updates the user`() {
        val newUser = testUser.copy(displayName = "NewName")
        val action = ChatViewModel.Action.UpdateUser(user = newUser)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals("NewName", result.user.displayName)
    }

    @Test
    fun `UpdateUser from Initial is a no-op`() {
        val action = ChatViewModel.Action.UpdateUser(user = testUser)

        val result = reducer.reduce(action, ChatViewModel.State.Initial)

        assertIs<ChatViewModel.State.Initial>(result)
    }

    @Test
    fun `UpdateUser from Failed is a no-op`() {
        val failedState = ChatViewModel.State.Failed(throwable = RuntimeException("fail"))
        val action = ChatViewModel.Action.UpdateUser(user = testUser)

        val result = reducer.reduce(action, failedState)

        assertIs<ChatViewModel.State.Failed>(result)
    }

    // endregion

    // region Action: UpdateStreamDetails

    @Test
    fun `UpdateStreamDetails while Chatting sets the stream`() {
        val stream = Stream(
            id = "stream-1",
            userId = "user-123",
            category = StreamCategory(id = "cat-1", name = "Just Chatting"),
            title = "Live stream",
            viewerCount = 100,
            startedAt = Instant.fromEpochMilliseconds(5000),
        )
        val action = ChatViewModel.Action.UpdateStreamDetails(stream = stream)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(stream, result.stream)
    }

    @Test
    fun `UpdateStreamDetails when not Chatting is a no-op`() {
        val stream = Stream(
            id = "stream-1",
            userId = "user-123",
            category = null,
            title = "Live",
            viewerCount = 0,
            startedAt = Instant.fromEpochMilliseconds(5000),
        )
        val action = ChatViewModel.Action.UpdateStreamDetails(stream = stream)

        val result = reducer.reduce(action, ChatViewModel.State.Initial)

        assertIs<ChatViewModel.State.Initial>(result)
    }

    // endregion

    // region Action: UpdateEmotes

    @Test
    fun `UpdateEmotes sets new values`() {
        val emotes = persistentListOf<fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem>()
        val badges = persistentListOf<fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge>()
        val cheerEmotes = persistentMapOf<String, Emote>()

        val action = ChatViewModel.Action.UpdateEmotes(
            pickableEmotes = emotes,
            globalBadges = badges,
            channelBadges = badges,
            cheerEmotes = cheerEmotes,
        )

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(emotes, result.pickableEmotes)
        assertEquals(badges, result.globalBadges)
        assertEquals(badges, result.channelBadges)
        assertEquals(cheerEmotes, result.cheerEmotes)
    }

    @Test
    fun `UpdateEmotes preserves existing values when new ones are null`() {
        val existingBadges = persistentListOf(
            fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge(
                setId = "sub",
                version = "1",
                urls = EmoteUrls("https://example.com/badge.png"),
            ),
        )
        val existingCheerEmotes = persistentMapOf(
            "Cheer1" to Emote(name = "Cheer1", urls = EmoteUrls("https://example.com/cheer.png")),
        )
        val state = testChattingState.copy(
            globalBadges = existingBadges,
            channelBadges = existingBadges,
            cheerEmotes = existingCheerEmotes,
        )

        val action = ChatViewModel.Action.UpdateEmotes(
            pickableEmotes = persistentListOf(),
            globalBadges = null,
            channelBadges = null,
            cheerEmotes = null,
        )

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(existingBadges, result.globalBadges)
        assertEquals(existingBadges, result.channelBadges)
        assertEquals(existingCheerEmotes, result.cheerEmotes)
    }

    // endregion

    // region Action: AddMessages

    @Test
    fun `AddMessages adds messages to state`() {
        val msg = createMessage(messageId = "msg-1")
        val action = ChatViewModel.Action.AddMessages(messages = listOf(msg))

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(1, result.chatMessages.size)
        assertEquals("msg-1", result.chatMessages[0].body?.messageId)
    }

    @Test
    fun `AddMessages deduplicates messages`() {
        val msg = createMessage(messageId = "msg-1")
        val stateWithMsg = testChattingState.copy(
            chatMessages = persistentListOf(msg),
        )
        val action = ChatViewModel.Action.AddMessages(messages = listOf(msg))

        val result = reducer.reduce(action, stateWithMsg)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(1, result.chatMessages.size)
    }

    @Test
    fun `AddMessages updates chatters`() {
        val msg = createMessage(
            messageId = "msg-1",
            chatterId = "new-chatter",
            chatterLogin = "newchatter",
            chatterDisplayName = "NewChatter",
        )
        val action = ChatViewModel.Action.AddMessages(messages = listOf(msg))

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        val chatterIds = result.chatters.map { it.id }.toSet()
        assertEquals(true, "new-chatter" in chatterIds)
    }

    @Test
    fun `AddMessages updates lastSentMessageInstant for own messages`() {
        val timestamp = Instant.fromEpochMilliseconds(99999)
        val msg = createMessage(
            messageId = "msg-1",
            chatterId = testAppUser.userId,
            chatterLogin = testAppUser.userLogin,
            chatterDisplayName = "AppUser",
            timestamp = timestamp,
        )
        val action = ChatViewModel.Action.AddMessages(messages = listOf(msg))

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(timestamp, result.lastSentMessageInstant)
    }

    @Test
    fun `AddMessages when not Chatting is a no-op`() {
        val msg = createMessage(messageId = "msg-1")
        val action = ChatViewModel.Action.AddMessages(messages = listOf(msg))

        val result = reducer.reduce(action, ChatViewModel.State.Initial)

        assertIs<ChatViewModel.State.Initial>(result)
    }

    // endregion

    // region Action: ChangeConnectionStatus

    @Test
    fun `ChangeConnectionStatus updates the connection status`() {
        val status = ConnectionStatus(isAlive = true, registeredListeners = 2)
        val action = ChatViewModel.Action.ChangeConnectionStatus(connectionStatus = status)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(status, result.connectionStatus)
    }

    @Test
    fun `ChangeConnectionStatus when not Chatting is a no-op`() {
        val action = ChatViewModel.Action.ChangeConnectionStatus(
            connectionStatus = ConnectionStatus(isAlive = true, registeredListeners = 1),
        )

        val result = reducer.reduce(action, ChatViewModel.State.Initial)

        assertIs<ChatViewModel.State.Initial>(result)
    }

    // endregion

    // region Action: ChangeUserState

    @Test
    fun `ChangeUserState updates user state`() {
        val userState = ChatListItem.UserState(emoteSets = persistentListOf("set1", "set2"))
        val action = ChatViewModel.Action.ChangeUserState(userState = userState)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(userState, result.userState)
    }

    // endregion

    // region Action: ChangeRoomState

    @Test
    fun `ChangeRoomState merges delta into existing room state`() {
        val state = testChattingState.copy(
            roomState = RoomState(isEmoteOnly = false, isSubOnly = false),
        )
        val delta = ChatListItem.RoomStateDelta(isEmoteOnly = true)
        val action = ChatViewModel.Action.ChangeRoomState(delta = delta)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(true, result.roomState.isEmoteOnly)
        assertEquals(false, result.roomState.isSubOnly)
    }

    @Test
    fun `ChangeRoomState partial update preserves existing values`() {
        val state = testChattingState.copy(
            roomState = RoomState(
                slowModeDuration = 30.seconds,
                isEmoteOnly = true,
            ),
        )
        val delta = ChatListItem.RoomStateDelta(isSubOnly = true)
        val action = ChatViewModel.Action.ChangeRoomState(delta = delta)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(true, result.roomState.isSubOnly)
        assertEquals(true, result.roomState.isEmoteOnly)
        assertEquals(30.seconds, result.roomState.slowModeDuration)
    }

    // endregion

    // region Action: RemoveContent

    @Test
    fun `RemoveContent appends to the removed content list`() {
        val removed = ChatListItem.RemoveContent(
            upUntil = Instant.fromEpochMilliseconds(5000),
            matchingUserId = "banned-user",
        )
        val action = ChatViewModel.Action.RemoveContent(removedContent = removed)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(1, result.removedContent.size)
        assertEquals(removed, result.removedContent[0])
    }

    // endregion

    // region Action: ChangeRecentEmotes

    @Test
    fun `ChangeRecentEmotes replaces the recent emotes list`() {
        val emotes = listOf(
            Emote(name = "Kappa", urls = EmoteUrls("https://example.com/kappa.png")),
        )
        val action = ChatViewModel.Action.ChangeRecentEmotes(recentEmotes = emotes)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(emotes, result.recentEmotes)
    }

    // endregion

    // region Action: UpdatePoll

    @Test
    fun `UpdatePoll sets the poll in ongoing events`() {
        val poll = Poll(
            pollId = "poll-1",
            status = Poll.Status.Active,
            title = "Test poll",
            startedAt = Instant.fromEpochMilliseconds(1000),
            choices = emptyList(),
            duration = 5.minutes,
            remainingDuration = 3.minutes,
            totalVoters = 0,
            votes = Poll.Votes(total = 0, bits = 0, channelPoints = 0, base = 0),
        )
        val action = ChatViewModel.Action.UpdatePoll(poll = poll)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(poll, result.ongoingEvents.poll)
    }

    // endregion

    // region Action: UpdatePrediction

    @Test
    fun `UpdatePrediction sets the prediction in ongoing events`() {
        val prediction = Prediction(
            id = "pred-1",
            title = "Test prediction",
            status = Prediction.Status.Active,
            createdAt = Instant.fromEpochMilliseconds(1000),
            outcomes = emptyList(),
            predictionWindow = 5.minutes,
        )
        val action = ChatViewModel.Action.UpdatePrediction(prediction = prediction)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(prediction, result.ongoingEvents.prediction)
    }

    // endregion

    // region Action: UpdateStreamMetadata

    @Test
    fun `UpdateStreamMetadata updates the stream title`() {
        val stream = Stream(
            id = "stream-1",
            userId = "user-123",
            category = null,
            title = "Old title",
            viewerCount = 100,
            startedAt = Instant.fromEpochMilliseconds(5000),
        )
        val state = testChattingState.copy(stream = stream)
        val action = ChatViewModel.Action.UpdateStreamMetadata(streamTitle = "New title")

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals("New title", result.stream?.title)
        assertEquals(100, result.stream?.viewerCount)
    }

    @Test
    fun `UpdateStreamMetadata updates the viewer count`() {
        val stream = Stream(
            id = "stream-1",
            userId = "user-123",
            category = null,
            title = "Title",
            viewerCount = 100,
            startedAt = Instant.fromEpochMilliseconds(5000),
        )
        val state = testChattingState.copy(stream = stream)
        val action = ChatViewModel.Action.UpdateStreamMetadata(viewerCount = 500)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(500, result.stream?.viewerCount)
        assertEquals("Title", result.stream?.title)
    }

    @Test
    fun `UpdateStreamMetadata updates the category`() {
        val stream = Stream(
            id = "stream-1",
            userId = "user-123",
            category = null,
            title = "Title",
            viewerCount = 100,
            startedAt = Instant.fromEpochMilliseconds(5000),
        )
        val state = testChattingState.copy(stream = stream)
        val newCategory = StreamCategory(id = "cat-1", name = "Gaming")
        val action = ChatViewModel.Action.UpdateStreamMetadata(streamCategory = newCategory)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(newCategory, result.stream?.category)
    }

    @Test
    fun `UpdateStreamMetadata with no existing stream is a no-op`() {
        val action = ChatViewModel.Action.UpdateStreamMetadata(streamTitle = "New title")

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertNull(result.stream)
    }

    // endregion

    // region Action: AddRichEmbed

    @Test
    fun `AddRichEmbed adds embed to the map keyed by messageId`() {
        val embed = ChatListItem.RichEmbed(
            messageId = "msg-1",
            title = "Embed title",
            requestUrl = "https://example.com",
            thumbnailUrl = "https://example.com/thumb.png",
            authorName = "Author",
            channelName = "Channel",
        )
        val action = ChatViewModel.Action.AddRichEmbed(richEmbed = embed)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(embed, result.richEmbeds["msg-1"])
    }

    // endregion

    // region Action: UpdateChatterPronouns

    @Test
    fun `UpdateChatterPronouns merges pronouns into existing map`() {
        val chatter = Chatter(id = "c1", login = "c1", displayName = "C1")
        val pronoun = fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun(
            id = "they",
            nominative = "they",
            objective = "them",
            isSingular = true,
        )
        val action = ChatViewModel.Action.UpdateChatterPronouns(
            pronouns = mapOf(chatter to pronoun),
        )

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(pronoun, result.pronouns[chatter])
    }

    // endregion

    // region Action: UpdatePinnedMessage

    @Test
    fun `UpdatePinnedMessage with null clears the pinned message`() {
        val action = ChatViewModel.Action.UpdatePinnedMessage(pinnedMessage = null)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertNull(result.ongoingEvents.pinnedMessage)
    }

    @Test
    fun `UpdatePinnedMessage with matching message pins it`() {
        val msg = createMessage(messageId = "pinned-msg-1")
        val state = testChattingState.copy(
            chatMessages = persistentListOf(msg),
        )
        val pinnedMessage = PinnedMessage(
            pinId = "pin-1",
            pinnedBy = PinnedMessage.User(userId = "mod-1", displayName = "Mod"),
            message = PinnedMessage.Message(
                messageId = "pinned-msg-1",
                sender = PinnedMessage.User(userId = "chatter-1", displayName = "Chatter1"),
                content = PinnedMessage.Message.Content(text = "hello"),
                startsAt = Instant.fromEpochMilliseconds(1000),
                endsAt = Instant.fromEpochMilliseconds(9000),
            ),
        )
        val action = ChatViewModel.Action.UpdatePinnedMessage(pinnedMessage = pinnedMessage)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(msg, result.ongoingEvents.pinnedMessage?.message)
        assertEquals(
            Instant.fromEpochMilliseconds(9000),
            result.ongoingEvents.pinnedMessage?.endsAt,
        )
    }

    @Test
    fun `UpdatePinnedMessage with no matching message clears the pinned message`() {
        val pinnedMessage = PinnedMessage(
            pinId = "pin-1",
            pinnedBy = PinnedMessage.User(userId = "mod-1", displayName = "Mod"),
            message = PinnedMessage.Message(
                messageId = "nonexistent-msg",
                sender = PinnedMessage.User(userId = "chatter-1", displayName = "Chatter1"),
                content = PinnedMessage.Message.Content(text = "hello"),
                startsAt = Instant.fromEpochMilliseconds(1000),
                endsAt = Instant.fromEpochMilliseconds(9000),
            ),
        )
        val action = ChatViewModel.Action.UpdatePinnedMessage(pinnedMessage = pinnedMessage)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertNull(result.ongoingEvents.pinnedMessage)
    }

    // endregion

    // region Action: UpdateRaidAnnouncement

    @Test
    fun `UpdateRaidAnnouncement sets the outgoing raid`() {
        val raid = Raid.Preparing(
            targetId = "target-1",
            targetLogin = "target",
            targetDisplayName = "Target",
            targetProfileImageUrl = null,
            viewerCount = 50,
        )
        val action = ChatViewModel.Action.UpdateRaidAnnouncement(raid = raid)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(raid, result.ongoingEvents.outgoingRaid)
    }

    @Test
    fun `UpdateRaidAnnouncement with null clears the outgoing raid`() {
        val state = testChattingState.copy(
            ongoingEvents = OngoingEvents(
                outgoingRaid = Raid.Preparing(
                    targetId = "t",
                    targetLogin = "t",
                    targetDisplayName = "T",
                    targetProfileImageUrl = null,
                    viewerCount = 10,
                ),
            ),
        )
        val action = ChatViewModel.Action.UpdateRaidAnnouncement(raid = null)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertNull(result.ongoingEvents.outgoingRaid)
    }

    // endregion

    // region Action: ShowUserInfo

    @Test
    fun `ShowUserInfo sets the user id to show info for`() {
        val action = ChatViewModel.Action.ShowUserInfo(userId = "some-user")

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals("some-user", result.showInfoForUserId)
    }

    @Test
    fun `ShowUserInfo with null clears the shown user info`() {
        val state = testChattingState.copy(showInfoForUserId = "some-user")
        val action = ChatViewModel.Action.ShowUserInfo(userId = null)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertNull(result.showInfoForUserId)
    }

    // endregion

    // region Action: UpdateStreamInfoVisibility

    @Test
    fun `UpdateStreamInfoVisibility sets stream info visible`() {
        val action = ChatViewModel.Action.UpdateStreamInfoVisibility(isVisible = true)

        val result = reducer.reduce(action, testChattingState)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(true, result.isStreamInfoVisible)
    }

    @Test
    fun `UpdateStreamInfoVisibility sets stream info hidden`() {
        val state = testChattingState.copy(isStreamInfoVisible = true)
        val action = ChatViewModel.Action.UpdateStreamInfoVisibility(isVisible = false)

        val result = reducer.reduce(action, state)

        assertIs<ChatViewModel.State.Chatting>(result)
        assertEquals(false, result.isStreamInfoVisible)
    }

    // endregion

    // region InputAction: ClearAfterSubmit

    @Test
    fun `ClearAfterSubmit clears message and stores last sent message`() {
        val inputState = ChatViewModel.InputState(
            message = "hello world",
            selectionRange = 0..11,
            replyingTo = createMessage(messageId = "reply-to"),
        )
        val action = ChatViewModel.InputAction.ClearAfterSubmit(sentMessage = "hello world")

        val result = reducer.reduce(action, inputState)

        assertEquals("", result.message)
        assertEquals("hello world", result.lastSentMessage)
        assertEquals(0..0, result.selectionRange)
        assertNull(result.replyingTo)
    }

    // endregion

    // region InputAction: ChangeMessageInput

    @Test
    fun `ChangeMessageInput updates message and selection range`() {
        val inputState = ChatViewModel.InputState()
        val action = ChatViewModel.InputAction.ChangeMessageInput(
            message = "new text",
            selectionRange = 4..4,
        )

        val result = reducer.reduce(action, inputState)

        assertEquals("new text", result.message)
        assertEquals(4..4, result.selectionRange)
    }

    // endregion

    // region InputAction: AppendEmote

    @Test
    fun `AppendEmote appends emote name to empty input`() {
        val inputState = ChatViewModel.InputState(
            message = "",
            selectionRange = 0..0,
        )
        val emote = Emote(name = "Kappa", urls = EmoteUrls("https://example.com/kappa.png"))
        val action = ChatViewModel.InputAction.AppendEmote(emote = emote, autocomplete = false)

        val result = reducer.reduce(action, inputState)

        assertEquals("Kappa ", result.message)
        assertEquals(6..6, result.selectionRange)
    }

    @Test
    fun `AppendEmote appends emote name to existing input`() {
        val inputState = ChatViewModel.InputState(
            message = "hello ",
            selectionRange = 6..6,
        )
        val emote = Emote(name = "PogChamp", urls = EmoteUrls("https://example.com/pog.png"))
        val action = ChatViewModel.InputAction.AppendEmote(emote = emote, autocomplete = false)

        val result = reducer.reduce(action, inputState)

        assertEquals("hello PogChamp ", result.message)
        assertEquals(15..15, result.selectionRange)
    }

    @Test
    fun `AppendEmote with autocomplete replaces the last word`() {
        val inputState = ChatViewModel.InputState(
            message = "hello Kap",
            selectionRange = 9..9,
        )
        val emote = Emote(name = "Kappa", urls = EmoteUrls("https://example.com/kappa.png"))
        val action = ChatViewModel.InputAction.AppendEmote(emote = emote, autocomplete = true)

        val result = reducer.reduce(action, inputState)

        assertEquals("hello Kappa ", result.message)
        assertEquals(12..12, result.selectionRange)
    }

    // endregion

    // region InputAction: AppendChatter

    @Test
    fun `AppendChatter appends at-display-name to input`() {
        val inputState = ChatViewModel.InputState(
            message = "",
            selectionRange = 0..0,
        )
        val chatter = Chatter(id = "c1", login = "testchatter", displayName = "TestChatter")
        val action = ChatViewModel.InputAction.AppendChatter(chatter = chatter, autocomplete = false)

        val result = reducer.reduce(action, inputState)

        assertEquals("@TestChatter ", result.message)
        assertEquals(13..13, result.selectionRange)
    }

    @Test
    fun `AppendChatter with autocomplete replaces the last word`() {
        val inputState = ChatViewModel.InputState(
            message = "hello @Test",
            selectionRange = 11..11,
        )
        val chatter = Chatter(id = "c1", login = "testchatter", displayName = "TestChatter")
        val action = ChatViewModel.InputAction.AppendChatter(chatter = chatter, autocomplete = true)

        val result = reducer.reduce(action, inputState)

        assertEquals("hello @TestChatter ", result.message)
        assertEquals(19..19, result.selectionRange)
    }

    // endregion

    // region InputAction: ReplyToMessage

    @Test
    fun `ReplyToMessage sets the message being replied to`() {
        val inputState = ChatViewModel.InputState()
        val msg = createMessage(messageId = "msg-1")
        val action = ChatViewModel.InputAction.ReplyToMessage(chatListItem = msg)

        val result = reducer.reduce(action, inputState)

        assertEquals(msg, result.replyingTo)
    }

    @Test
    fun `ReplyToMessage with null clears the reply`() {
        val inputState = ChatViewModel.InputState(
            replyingTo = createMessage(messageId = "msg-1"),
        )
        val action = ChatViewModel.InputAction.ReplyToMessage(chatListItem = null)

        val result = reducer.reduce(action, inputState)

        assertNull(result.replyingTo)
    }

    // endregion

    // region InputAction: UpdateAutoCompleteItems

    @Test
    fun `UpdateAutoCompleteItems sets the autocomplete items`() {
        val inputState = ChatViewModel.InputState()
        val items = persistentListOf<AutoCompleteItem>(
            AutoCompleteItem.Emote(
                emote = Emote(name = "Kappa", urls = EmoteUrls("https://example.com/kappa.png")),
            ),
        )
        val action = ChatViewModel.InputAction.UpdateAutoCompleteItems(items = items)

        val result = reducer.reduce(action, inputState)

        assertEquals(items, result.autoCompleteItems)
    }

    // endregion

    // region InputAction: ReplaceInputWithLastSentMessage

    @Test
    fun `ReplaceInputWithLastSentMessage replaces empty input with last sent message`() {
        val inputState = ChatViewModel.InputState(
            message = "",
            lastSentMessage = "previous message",
        )
        val action = ChatViewModel.InputAction.ReplaceInputWithLastSentMessage

        val result = reducer.reduce(action, inputState)

        assertEquals("previous message", result.message)
        assertEquals("previous message", result.lastSentMessage)
        assertEquals(16..16, result.selectionRange)
    }

    @Test
    fun `ReplaceInputWithLastSentMessage with non-empty input is a no-op`() {
        val inputState = ChatViewModel.InputState(
            message = "some text",
            lastSentMessage = "previous message",
            selectionRange = 9..9,
        )
        val action = ChatViewModel.InputAction.ReplaceInputWithLastSentMessage

        val result = reducer.reduce(action, inputState)

        assertEquals("some text", result.message)
        assertEquals(9..9, result.selectionRange)
    }

    @Test
    fun `ReplaceInputWithLastSentMessage with no last message is a no-op`() {
        val inputState = ChatViewModel.InputState(
            message = "",
            lastSentMessage = null,
        )
        val action = ChatViewModel.InputAction.ReplaceInputWithLastSentMessage

        val result = reducer.reduce(action, inputState)

        assertEquals("", result.message)
        assertNull(result.lastSentMessage)
    }

    // endregion
}
