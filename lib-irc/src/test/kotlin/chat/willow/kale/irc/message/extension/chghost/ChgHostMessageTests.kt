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
package chat.willow.kale.irc.message.extension.chghost

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.Prefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ChgHostMessageTests {

    private lateinit var messageParser: ChgHostMessage.Message.Parser
    private lateinit var messageSerialiser: ChgHostMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = ChgHostMessage.Message.Parser
        messageSerialiser = ChgHostMessage.Message.Serialiser
    }

    @Test fun test_parse_SanityCheck() {
        val message = messageParser.parse(IrcMessage(command = "CHGHOST", prefix = "testnick!testuser@testhost", parameters = listOf("newuser", "newhost")))

        assertEquals(ChgHostMessage.Message(source = Prefix(nick = "testnick", user = "testuser", host = "testhost"), newUser = "newuser", newHost = "newhost"), message)
    }

    @Test fun test_parse_TooFewParameters_ReturnsNull() {
        val messageOne = messageParser.parse(IrcMessage(command = "CHGHOST", prefix = "testnick!testuser@testhost", parameters = listOf("newuser")))
        val messageTwo = messageParser.parse(IrcMessage(command = "CHGHOST", prefix = "testnick!testuser@testhost", parameters = listOf()))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_parse_MissingPrefix_ReturnsNull() {
        val message = messageParser.parse(IrcMessage(command = "CHGHOST", prefix = null, parameters = listOf("newuser", "newhost")))

        assertNull(message)
    }

    @Test fun test_serialise_SanityCheck() {
        val message = messageSerialiser.serialise(ChgHostMessage.Message(source = Prefix(nick = "testnick", user = "testuser", host = "testhost"), newUser = "newuser", newHost = "newhost"))

        assertEquals(IrcMessage(command = "CHGHOST", prefix = "testnick!testuser@testhost", parameters = listOf("newuser", "newhost")), message)
    }
}
