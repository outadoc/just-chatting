package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class ChatListItemMessageExtTest {
    private val sentAt = Instant.fromEpochSeconds(1_000)

    private fun message(
        text: String = "hello",
        messageId: String = "msg-1",
        chatterId: String = "chatter-1",
        embeddedEmotes: List<Emote> = emptyList(),
        badges: List<Badge> = emptyList(),
        sourceBadges: List<Badge> = emptyList(),
        sourceRoomId: String? = null,
    ) = ChatListItem.Message.Simple(
        body =
            ChatListItem.Message.Body(
                messageId = messageId,
                message = text,
                chatter = Chatter(id = chatterId, login = "chatter", displayName = "Chatter"),
                embeddedEmotes = persistentListOf(*embeddedEmotes.toTypedArray()),
                badges = persistentListOf(*badges.toTypedArray()),
                sourceBadges = persistentListOf(*sourceBadges.toTypedArray()),
                sourceRoomId = sourceRoomId,
            ),
        timestamp = sentAt,
    )

    private fun emote(name: String) = Emote(name = name, urls = EmoteUrls(url = "https://example.com/$name.png"))

    // region isRedacted

    @Test
    fun `message is not redacted without removal rules`() {
        assertFalse(message().isRedacted(emptyList()))
    }

    @Test
    fun `clearing the whole chat redacts earlier messages only`() {
        val clear = ChatListItem.RemoveContent(upUntil = sentAt + 1.seconds)
        val earlierClear = ChatListItem.RemoveContent(upUntil = sentAt - 1.seconds)

        assertTrue(message().isRedacted(listOf(clear)))
        assertFalse(message().isRedacted(listOf(earlierClear)))
    }

    @Test
    fun `deleting a message redacts that message only`() {
        val rule = ChatListItem.RemoveContent(upUntil = sentAt + 1.seconds, matchingMessageId = "msg-1")

        assertTrue(message(messageId = "msg-1").isRedacted(listOf(rule)))
        assertFalse(message(messageId = "msg-2").isRedacted(listOf(rule)))
    }

    @Test
    fun `banning a chatter redacts their messages only`() {
        val rule = ChatListItem.RemoveContent(upUntil = sentAt + 1.seconds, matchingUserId = "chatter-1")

        assertTrue(message(chatterId = "chatter-1").isRedacted(listOf(rule)))
        assertFalse(message(chatterId = "chatter-2").isRedacted(listOf(rule)))
    }

    // endregion

    // region usedEmotes

    @Test
    fun `used emotes include embedded and chat emotes, each once`() {
        val kappa = emote("Kappa")
        val pog = emote("Pog")

        val used =
            message(text = "Kappa Pog Pog hello", embeddedEmotes = listOf(kappa))
                .body
                .usedEmotes(emotes = mapOf("Pog" to pog, "Kappa" to kappa))

        assertEquals(listOf(kappa, pog), used)
    }

    // endregion

    // region displayedBadges

    @Test
    fun `chat badges are displayed for regular messages`() {
        val badges = listOf(Badge(id = "subscriber", version = "12"))

        assertEquals(
            DisplayedBadges(badges = badges, sourceRoomId = null),
            message(badges = badges).body.displayedBadges,
        )
    }

    @Test
    fun `source channel badges are displayed for messages from a shared chat`() {
        val sourceBadges = listOf(Badge(id = "moderator", version = "1"))

        assertEquals(
            DisplayedBadges(badges = sourceBadges, sourceRoomId = "room-2"),
            message(
                badges = listOf(Badge(id = "subscriber", version = "12")),
                sourceBadges = sourceBadges,
                sourceRoomId = "room-2",
            ).body.displayedBadges,
        )
    }

    // endregion
}
