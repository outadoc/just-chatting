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
package chat.willow.kale.irc.message.extension.away_notify

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.Prefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AwayMessageTests {

    lateinit var messageParser: AwayMessage.Message.Parser
    lateinit var messageSerialiser: AwayMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = AwayMessage.Message.Parser
        messageSerialiser = AwayMessage.Message.Serialiser
    }

    @Test fun test_parse_SourceAndMessage() {
        val message = messageParser.parse(IrcMessage(command = "AWAY", prefix = "nickname", parameters = listOf("test away message")))

        assertEquals(AwayMessage.Message(source = Prefix(nick = "nickname"), message = "test away message"), message)
    }

    @Test fun test_parse_SourceWithoutMessage() {
        val message = messageParser.parse(IrcMessage(command = "AWAY", prefix = "nickname", parameters = listOf()))

        assertEquals(AwayMessage.Message(source = Prefix(nick = "nickname"), message = null), message)
    }

    @Test fun test_parse_MissingPrefix_ReturnsNull() {
        val message = messageParser.parse(IrcMessage(command = "AWAY", parameters = listOf("away")))

        assertNull(message)
    }

    @Test fun test_serialise_SourceAndMessage() {
        val message = messageSerialiser.serialise(AwayMessage.Message(source = Prefix(nick = "nickname"), message = "test away message"))

        assertEquals(IrcMessage(command = "AWAY", prefix = "nickname", parameters = listOf("test away message")), message)
    }

    @Test fun test_serialise_SourceWithoutMessage() {
        val message = messageSerialiser.serialise(AwayMessage.Message(source = Prefix(nick = "nickname"), message = null))

        assertEquals(IrcMessage(command = "AWAY", prefix = "nickname", parameters = listOf()), message)
    }
}
