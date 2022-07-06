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
package chat.willow.kale.irc.message.extension.monitor.rpl

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.Prefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RplMonListIsFullMessageTests {

    private lateinit var messageParser: RplMonListIsFullMessage.Message.Parser
    private lateinit var messageSerialiser: RplMonListIsFullMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = RplMonListIsFullMessage.Message.Parser
        messageSerialiser = RplMonListIsFullMessage.Message.Serialiser
    }

    @Test fun test_parse_ValidPrefix_SingleTarget() {
        val message = messageParser.parse(IrcMessage(command = "734", prefix = "server", parameters = listOf("nick", "100", "someone!user@somewhere", "message")))

        assertEquals(RplMonListIsFullMessage.Message(prefix = Prefix(nick = "server"), nick = "nick", limit = "100", targets = listOf(Prefix(nick = "someone", user = "user", host = "somewhere")), message = "message"), message)
    }

    @Test fun test_parse_ValidPrefix_MultipleTargets() {
        val message = messageParser.parse(IrcMessage(command = "734", prefix = "server", parameters = listOf("nick", "100", "someone!user@somewhere,someone-else", "message")))

        assertEquals(RplMonListIsFullMessage.Message(prefix = Prefix(nick = "server"), nick = "nick", limit = "100", targets = listOf(Prefix(nick = "someone", user = "user", host = "somewhere"), Prefix(nick = "someone-else")), message = "message"), message)
    }

    @Test fun test_parse_MissingPrefix() {
        val message = messageParser.parse(IrcMessage(command = "734", prefix = null, parameters = listOf("nick", "100", "someone!user@somewhere,someone-else", "message")))

        assertNull(message)
    }

    @Test fun test_parse_InvalidPrefix() {
        val message = messageParser.parse(IrcMessage(command = "734", prefix = "!!!", parameters = listOf("nick", "100", "someone!user@somewhere,someone-else", "message")))

        assertNull(message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "734", prefix = "server", parameters = listOf("*")))
        val messageTwo = messageParser.parse(IrcMessage(command = "734", prefix = "server", parameters = listOf()))
        val messageThree = messageParser.parse(IrcMessage(command = "734", prefix = "server", parameters = listOf("nick", "100")))

        assertNull(messageOne)
        assertNull(messageTwo)
        assertNull(messageThree)
    }

    @Test fun test_serialise_SingleTarget() {
        val message = messageSerialiser.serialise(RplMonListIsFullMessage.Message(prefix = Prefix(nick = "server"), nick = "nick", limit = "100", targets = listOf(Prefix(nick = "someone")), message = "message"))

        assertEquals(IrcMessage(command = "734", prefix = "server", parameters = listOf("nick", "100", "someone", "message")), message)
    }

    @Test fun test_serialise_MultipleTargets() {
        val message = messageSerialiser.serialise(RplMonListIsFullMessage.Message(prefix = Prefix(nick = "server"), nick = "nick", limit = "100", targets = listOf(Prefix(nick = "someone"), Prefix(nick = "someone-else", user = "user", host = "somewhere")), message = "message"))

        assertEquals(IrcMessage(command = "734", prefix = "server", parameters = listOf("nick", "100", "someone,someone-else!user@somewhere", "message")), message)
    }
}
