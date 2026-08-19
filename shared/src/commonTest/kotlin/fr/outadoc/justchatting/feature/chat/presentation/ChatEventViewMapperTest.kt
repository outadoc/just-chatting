package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
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
    ): ChatEvent.Message.ChatMessage =
        ChatEvent.Message.ChatMessage(
            timestamp = Instant.fromEpochMilliseconds(1_000),
            id = id,
            userId = userId,
            userLogin = userLogin,
            userName = userName,
            message = text,
            color = null,
            embeddedEmotes = emptyList(),
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
}
