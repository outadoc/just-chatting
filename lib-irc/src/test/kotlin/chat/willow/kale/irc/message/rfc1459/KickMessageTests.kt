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
import chat.willow.kale.irc.prefix.prefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class KickMessageTests {

    private lateinit var messageParser: KickMessage.Message.Parser
    private lateinit var messageSerialiser: KickMessage.Command.Serialiser

    @Before fun setUp() {
        messageParser = KickMessage.Message.Parser
        messageSerialiser = KickMessage.Command.Serialiser
    }

    @Test fun test_parse_OneChannel_OneUser_NoComment() {
        val message = messageParser.parse(IrcMessage(command = "KICK", prefix = "someone", parameters = listOf("#channel1", "user1")))

        assertEquals(KickMessage.Message(source = prefix("someone"), channels = listOf("#channel1"), users = listOf("user1")), message)
    }

    @Test fun test_parse_OneChannel_OneUser_WithComment() {
        val message = messageParser.parse(IrcMessage(command = "KICK", prefix = "someone", parameters = listOf("#channel1", "user1", "kicked!")))

        assertEquals(KickMessage.Message(source = prefix("someone"), channels = listOf("#channel1"), users = listOf("user1"), comment = "kicked!"), message)
    }

    @Test fun test_parse_MultipleChannels_MultipleUsers_NoComment() {
        val message = messageParser.parse(IrcMessage(command = "KICK", prefix = "someone", parameters = listOf("#channel1,#channel2,#channel3", "user1,user2,user3")))

        assertEquals(KickMessage.Message(source = prefix("someone"), channels = listOf("#channel1", "#channel2", "#channel3"), users = listOf("user1", "user2", "user3")), message)
    }

    @Test fun test_parse_MultipleChannels_MultipleUsers_WithComment() {
        val message = messageParser.parse(IrcMessage(command = "KICK", prefix = "someone", parameters = listOf("#channel1,#channel2,#channel3", "user1,user2,user3", "kicked!!")))

        assertEquals(KickMessage.Message(source = prefix("someone"), channels = listOf("#channel1", "#channel2", "#channel3"), users = listOf("user1", "user2", "user3"), comment = "kicked!!"), message)
    }

    @Test fun test_parse_MismatchedChannelsAndUsers() {
        val message = messageParser.parse(IrcMessage(command = "KICK", prefix = "someone", parameters = listOf("#channel1,#channel2", "user1,user2,user3")))

        assertNull(message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "KICK", prefix = "someone", parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "KICK", prefix = "someone", parameters = listOf("#channel1,#channel2")))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_parse_NoSource() {
        val message = messageParser.parse(IrcMessage(command = "KICK", prefix = null))

        assertNull(message)
    }

    @Test fun test_serialise_OneChannel_OneUser_NoComment() {
        val message = messageSerialiser.serialise(KickMessage.Command(channels = listOf("#channel1"), users = listOf("user1")))

        assertEquals(IrcMessage(command = "KICK", parameters = listOf("#channel1", "user1")), message)
    }

    @Test fun test_serialise_OneChannel_OneUser_WithComment() {
        val message = messageSerialiser.serialise(KickMessage.Command(channels = listOf("#channel1"), users = listOf("user1"), comment = "kicked!!!"))

        assertEquals(IrcMessage(command = "KICK", parameters = listOf("#channel1", "user1", "kicked!!!")), message)
    }

    @Test fun test_serialise_MultipleChannels_MultipleUsers_NoComment() {
        val message = messageSerialiser.serialise(KickMessage.Command(channels = listOf("#channel1", "#channel2"), users = listOf("user1", "user2")))

        assertEquals(IrcMessage(command = "KICK", parameters = listOf("#channel1,#channel2", "user1,user2")), message)
    }

    @Test fun test_serialise_MultipleChannels_MultipleUsers_WithComment() {
        val message = messageSerialiser.serialise(KickMessage.Command(channels = listOf("#channel1", "#channel2"), users = listOf("user1", "user2"), comment = "kicked!!!"))

        assertEquals(IrcMessage(command = "KICK", parameters = listOf("#channel1,#channel2", "user1,user2", "kicked!!!")), message)
    }

    @Test fun test_serialise_MismatchedChannelsAndUsers() {
        val message = messageSerialiser.serialise(KickMessage.Command(channels = listOf("#channel1", "#channel2"), users = listOf("user1")))

        assertEquals(IrcMessage(command = "KICK", parameters = listOf("#channel1,#channel2", "user1")), message)
    }
}
