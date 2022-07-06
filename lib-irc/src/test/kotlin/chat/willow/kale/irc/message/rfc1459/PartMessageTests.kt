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
package chat.willow.kale.irc.message.rfc1459

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.prefix.prefix
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PartMessageTests {

    private lateinit var messageParser: PartMessage.Message.Parser
    private lateinit var messageSerialiser: PartMessage.Command.Serialiser

    @Before fun setUp() {
        messageParser = PartMessage.Message.Parser
        messageSerialiser = PartMessage.Command.Serialiser
    }

    @Test fun test_parse_OneChannel() {
        val message = messageParser.parse(IrcMessage(command = "PART", prefix = "someone", parameters = listOf("channel1")))

        assertEquals(message, PartMessage.Message(source = prefix("someone"), channels = listOf("channel1")))
    }

    @Test fun test_parse_MultipleChannels() {
        val message = messageParser.parse(IrcMessage(command = "PART", prefix = "someone", parameters = listOf("channel1,channel2,channel3")))

        assertEquals(message, PartMessage.Message(source = prefix("someone"), channels = listOf("channel1", "channel2", "channel3")))
    }

    @Test fun test_parse_NoParameters() {
        val message = messageParser.parse(IrcMessage(command = "PART"))

        assertEquals(message, null)
    }

    @Test fun test_parse_SomeoneParted() {
        val message = messageParser.parse(IrcMessage(command = "PART", prefix = "someone@somewhere", parameters = listOf("#channel")))

        assertEquals(PartMessage.Message(source = Prefix(nick = "someone", host = "somewhere"), channels = listOf("#channel")), message)
    }

    @Test fun test_serialise_OneChannel() {
        val message = messageSerialiser.serialise(PartMessage.Command(channels = listOf("channel1")))

        assertEquals(message, IrcMessage(command = "PART", parameters = listOf("channel1")))
    }

    @Test fun test_serialise_MultipleChannels() {
        val message = messageSerialiser.serialise(PartMessage.Command(channels = listOf("channel1", "channel2")))

        assertEquals(message, IrcMessage(command = "PART", parameters = listOf("channel1,channel2")))
    }
}
