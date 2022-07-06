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

class RplMonOnlineMessageTests {

    private lateinit var messageParser: RplMonOnlineMessage.Message.Parser
    private lateinit var messageSerialiser: RplMonOnlineMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = RplMonOnlineMessage.Message.Parser
        messageSerialiser = RplMonOnlineMessage.Message.Serialiser
    }

    @Test fun test_parse_ValidPrefix_SingleTarget() {
        val message = messageParser.parse(IrcMessage(command = "730", prefix = "server", parameters = listOf("*", "someone!user@somewhere")))

        assertEquals(RplMonOnlineMessage.Message(prefix = Prefix(nick = "server"), nickOrStar = "*", targets = listOf(Prefix(nick = "someone", user = "user", host = "somewhere"))), message)
    }

    @Test fun test_parse_ValidPrefix_MultipleTargets() {
        val message = messageParser.parse(IrcMessage(command = "730", prefix = "server", parameters = listOf("*", "someone!user@somewhere,someone-else")))

        assertEquals(RplMonOnlineMessage.Message(prefix = Prefix(nick = "server"), nickOrStar = "*", targets = listOf(Prefix(nick = "someone", user = "user", host = "somewhere"), Prefix(nick = "someone-else"))), message)
    }

    @Test fun test_parse_MissingPrefix() {
        val message = messageParser.parse(IrcMessage(command = "730", prefix = null, parameters = listOf("*", "someone!user@somewhere,someone-else")))

        assertNull(message)
    }

    @Test fun test_parse_InvalidPrefix() {
        val message = messageParser.parse(IrcMessage(command = "730", prefix = "!!!", parameters = listOf("*", "someone!user@somewhere,someone-else")))

        assertNull(message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "730", prefix = "server", parameters = listOf("*")))
        val messageTwo = messageParser.parse(IrcMessage(command = "730", prefix = "server", parameters = listOf()))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_serialise_SingleTarget() {
        val message = messageSerialiser.serialise(RplMonOnlineMessage.Message(prefix = Prefix(nick = "server"), nickOrStar = "*", targets = listOf(Prefix(nick = "someone"))))

        assertEquals(IrcMessage(command = "730", prefix = "server", parameters = listOf("*", "someone")), message)
    }

    @Test fun test_serialise_MultipleTargets() {
        val message = messageSerialiser.serialise(RplMonOnlineMessage.Message(prefix = Prefix(nick = "server"), nickOrStar = "*", targets = listOf(Prefix(nick = "someone"), Prefix(nick = "someone-else", user = "user", host = "somewhere"))))

        assertEquals(IrcMessage(command = "730", prefix = "server", parameters = listOf("*", "someone,someone-else!user@somewhere")), message)
    }
}
