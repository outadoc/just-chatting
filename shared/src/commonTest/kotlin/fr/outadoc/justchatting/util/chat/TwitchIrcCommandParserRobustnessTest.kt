package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.feature.chat.data.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * A parse failure must never throw: an exception would close the chat socket, and
 * recent-message backfill would replay the same line on every reconnection.
 */
internal class TwitchIrcCommandParserRobustnessTest {
    private lateinit var parser: TwitchIrcCommandParser

    @BeforeTest
    fun before() {
        parser = TwitchIrcCommandParser(Clock.System)
    }

    @Test
    fun `PRIVMSG without user-id tag is dropped instead of throwing`() {
        assertNull(
            parser.parse(
                "@badges=;color=;display-name=ronni;emotes=;id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;tmi-sent-ts=1507246572675 " +
                    ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :hello",
            ),
        )
    }

    @Test
    fun `Emote indices out of message bounds are skipped`() {
        val message =
            parser.parse(
                "@emotes=25:0-4,40-60;id=id;tmi-sent-ts=1507246572675;user-id=1337 " +
                    ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa hi",
            )

        assertIs<ChatEvent.Message.ChatMessage>(message)
        assertEquals(listOf("Kappa"), message.embeddedEmotes.map { emote -> emote.name })
    }

    @Test
    fun `Malformed emote ranges are skipped`() {
        val message =
            parser.parse(
                "@emotes=25:0-4,notarange,7-;id=id;tmi-sent-ts=1507246572675;user-id=1337 " +
                    ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa hi",
            )

        assertIs<ChatEvent.Message.ChatMessage>(message)
        assertEquals(listOf("Kappa"), message.embeddedEmotes.map { emote -> emote.name })
    }

    @Test
    fun `Inverted emote range is skipped`() {
        val message =
            parser.parse(
                "@emotes=25:4-0;id=id;tmi-sent-ts=1507246572675;user-id=1337 " +
                    ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :Kappa hi",
            )

        assertIs<ChatEvent.Message.ChatMessage>(message)
        assertTrue(message.embeddedEmotes.isEmpty())
    }

    @Test
    fun `Escaped backslash followed by n is not unescaped to a line feed`() {
        // Wire value "a\\nb" is backslash-backslash-n-b, and must decode to "a\nb"
        // (a literal backslash followed by the letter n), not to a line feed.
        val message =
            parser.parse(
                "@display-name=a\\\\nb;id=id;tmi-sent-ts=1507246572675;user-id=1337 " +
                    ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :hello",
            )

        assertIs<ChatEvent.Message.ChatMessage>(message)
        assertEquals("a\\nb", message.userName)
    }

    @Test
    fun `Standard tag escapes are unescaped`() {
        val message =
            parser.parse(
                "@display-name=a\\sb\\:c\\\\d;id=id;tmi-sent-ts=1507246572675;user-id=1337 " +
                    ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :hello",
            )

        assertIs<ChatEvent.Message.ChatMessage>(message)
        assertEquals("a b;c\\d", message.userName)
    }

    @Test
    fun `Garbage lines do not throw`() {
        assertNull(parser.parse("@;;;===  :::"))
        assertNull(parser.parse("@"))
        assertNull(parser.parse(":"))
    }
}
