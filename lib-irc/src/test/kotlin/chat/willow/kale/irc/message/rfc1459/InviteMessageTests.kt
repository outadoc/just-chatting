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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class InviteMessageTests {

    private lateinit var messageParser: InviteMessage.Message.Parser
    private lateinit var messageSerialiser: InviteMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = InviteMessage.Message.Parser
        messageSerialiser = InviteMessage.Message.Serialiser
    }

    @Test fun test_parse_Source_User_Channel() {
        val message = messageParser.parse(IrcMessage(command = "INVITE", prefix = "someone", parameters = listOf("nickname", "#channel")))

        assertEquals(InviteMessage.Message(source = Prefix(nick = "someone"), user = "nickname", channel = "#channel"), message)
    }

    @Test fun test_parse_User_Channel_NoSource() {
        val message = messageParser.parse(IrcMessage(command = "INVITE", prefix = "someone", parameters = listOf("nickname", "#channel")))

        assertEquals(InviteMessage.Message(source = prefix("someone"), user = "nickname", channel = "#channel"), message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "INVITE", parameters = listOf("nickname")))
        val messageTwo = messageParser.parse(IrcMessage(command = "INVITE", parameters = listOf()))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_serialise_Source_User_Channel() {
        val message = messageSerialiser.serialise(InviteMessage.Message(source = Prefix(nick = "source"), user = "user", channel = "channel"))

        assertEquals(IrcMessage(command = "INVITE", prefix = "source", parameters = listOf("user", "channel")), message)
    }

    @Test fun test_serialise_User_Channel_NoSource() {
        val message = messageSerialiser.serialise(InviteMessage.Message(source = prefix("someone"), user = "user", channel = "channel"))

        assertEquals(IrcMessage(command = "INVITE", prefix = "someone", parameters = listOf("user", "channel")), message)
    }
}
