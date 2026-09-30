package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

internal class ChatEventViewMapperTest {
    private val mapper = ChatEventViewMapper()

    private fun chatMessageEvent(
        id: String = "message-id",
        text: String? = "hello world",
        userId: String = "user-id",
        userLogin: String = "user",
        userName: String = "User",
        embeddedEmotes: List<Emote> = emptyList(),
    ): ChatEvent.Message.ChatMessage =
        ChatEvent.Message.ChatMessage(
            timestamp = Instant.fromEpochMilliseconds(1_000),
            id = id,
            userId = userId,
            userLogin = userLogin,
            userName = userName,
            message = text,
            color = null,
            embeddedEmotes = embeddedEmotes,
            badges = null,
            rewardId = null,
            inReplyTo = null,
        )

    @Test
    fun `mention followed by text is treated as a reply`() {
        val event = chatMessageEvent(text = "@user1 hello")

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertEquals("hello", result.body.message)
        assertEquals<List<String>?>(listOf("user1"), result.body.inReplyTo?.mentions)
    }

    @Test
    fun `single mention with nothing else is not treated as a reply`() {
        val event = chatMessageEvent(text = "@user1")

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertNull(result.body.inReplyTo)
    }

    @Test
    fun `multiple mentions with nothing else are not treated as a reply`() {
        val event = chatMessageEvent(text = "@user1 @user2")

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertNull(result.body.inReplyTo)
    }

    @Test
    fun `multiple mentions followed by text are treated as a reply`() {
        val event = chatMessageEvent(text = "@user1 @user2 hello")

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertEquals("hello", result.body.message)
        assertEquals<List<String>?>(listOf("user1", "user2"), result.body.inReplyTo?.mentions)
    }

    private fun emote(name: String) = Emote(name = name, urls = EmoteUrls(url = "https://example.com/$name.png"))

    @Test
    fun `gigantified emote is taken out of the message text`() {
        val timide = emote("dfgTimide")
        val event =
            ChatEvent.Message.GigantifiedEmoteMessage(
                timestamp = Instant.fromEpochMilliseconds(1_000),
                userMessage = chatMessageEvent(text = "un gros panard dfgTimide", embeddedEmotes = listOf(timide)),
            )

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertEquals("un gros panard", result.body.message)
        assertEquals(timide, result.body.gigantifiedEmote)
    }

    @Test
    fun `only the last emote of a gigantified emote message is enlarged`() {
        val kappa = emote("Kappa")
        val pog = emote("Pog")
        val event =
            ChatEvent.Message.GigantifiedEmoteMessage(
                timestamp = Instant.fromEpochMilliseconds(1_000),
                userMessage = chatMessageEvent(text = "Pog Kappa Pog hello", embeddedEmotes = listOf(kappa, pog)),
            )

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertEquals("Pog Kappa hello", result.body.message)
        assertEquals(pog, result.body.gigantifiedEmote)
    }

    @Test
    fun `gigantified emote message with only an emote has no text left`() {
        val timide = emote("dfgTimide")
        val event =
            ChatEvent.Message.GigantifiedEmoteMessage(
                timestamp = Instant.fromEpochMilliseconds(1_000),
                userMessage = chatMessageEvent(text = "dfgTimide", embeddedEmotes = listOf(timide)),
            )

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertNull(result.body.message)
        assertEquals(timide, result.body.gigantifiedEmote)
    }

    @Test
    fun `regular message has no gigantified emote`() {
        val event = chatMessageEvent(text = "hello dfgTimide", embeddedEmotes = listOf(emote("dfgTimide")))

        val result = mapper.map(event).single()

        assertIs<ChatListItem.Message.Simple>(result)
        assertEquals("hello dfgTimide", result.body.message)
        assertNull(result.body.gigantifiedEmote)
    }
}
