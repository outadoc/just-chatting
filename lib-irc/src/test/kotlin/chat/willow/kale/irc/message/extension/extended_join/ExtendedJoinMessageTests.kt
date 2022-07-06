/*
 * Copyright © 2016, Sky Welch <license@bunnies.io>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package chat.willow.kale.irc.message.extension.extended_join

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.Prefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ExtendedJoinMessageTests {

    private lateinit var messageParser: ExtendedJoinMessage.Message.Parser
    private lateinit var messageSerialiser: ExtendedJoinMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = ExtendedJoinMessage.Message.Parser
        messageSerialiser = ExtendedJoinMessage.Message.Serialiser
    }

    @Test fun test_parse_UserHasAccount_ParsesAsExpected() {
        val message = messageParser.parse(IrcMessage(command = "JOIN", prefix = "nick", parameters = listOf("#channel", "account", "Real Name")))

        assertEquals(ExtendedJoinMessage.Message(source = Prefix(nick = "nick"), channel = "#channel", account = "account", realName = "Real Name"), message)
    }

    @Test fun test_parse_UserDoesNotHaveAccount_ParsesAsExpected() {
        val message = messageParser.parse(IrcMessage(command = "JOIN", prefix = "nick", parameters = listOf("#channel", "*", "Real Name")))

        assertEquals(ExtendedJoinMessage.Message(source = Prefix(nick = "nick"), channel = "#channel", account = null, realName = "Real Name"), message)
    }

    @Test fun test_parse_MissingPrefix_ReturnsNull() {
        val message = messageParser.parse(IrcMessage(command = "JOIN", prefix = null, parameters = listOf("#channel", "account", "Real Name")))

        assertNull(message)
    }

    @Test fun test_parse_TooFewParameters_ReturnsNull() {
        val messageOne = messageParser.parse(IrcMessage(command = "JOIN", prefix = null, parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "JOIN", prefix = null, parameters = listOf("#channel")))
        val messageThree = messageParser.parse(IrcMessage(command = "JOIN", prefix = null, parameters = listOf("#channel", "account")))

        assertNull(messageOne)
        assertNull(messageTwo)
        assertNull(messageThree)
    }

    @Test fun test_serialise_MessageHasAccount_SerialisesAsExpected() {
        val message = messageSerialiser.serialise(ExtendedJoinMessage.Message(source = Prefix(nick = "nick"), channel = "#channel", account = "account", realName = "Real Name"))

        assertEquals(IrcMessage(command = "JOIN", prefix = "nick", parameters = listOf("#channel", "account", "Real Name")), message)
    }

    @Test fun test_serialise_MessageDoesNotHaveAccount_SerialisesAsExpected() {
        val message = messageSerialiser.serialise(ExtendedJoinMessage.Message(source = Prefix(nick = "nick"), channel = "#channel", account = null, realName = "Real Name"))

        assertEquals(IrcMessage(command = "JOIN", prefix = "nick", parameters = listOf("#channel", "*", "Real Name")), message)
    }
}
