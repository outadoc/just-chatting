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
package chat.willow.kale.irc.message.extension.sasl

import chat.willow.kale.core.message.IrcMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthenticateMessageTests {

    private lateinit var messageParser: AuthenticateMessage.Message.Parser
    private lateinit var messageSerialiser: AuthenticateMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = AuthenticateMessage.Message.Parser
        messageSerialiser = AuthenticateMessage.Message.Serialiser
    }

    @Test fun test_parse_NotEmpty() {
        val message = messageParser.parse(IrcMessage(command = "AUTHENTICATE", parameters = listOf("base64payload")))

        assertEquals(message, AuthenticateMessage.Message(payload = "base64payload", isEmpty = false))
    }

    @Test fun test_parse_Empty() {
        val message = messageParser.parse(IrcMessage(command = "AUTHENTICATE", parameters = listOf("+")))

        assertEquals(message, AuthenticateMessage.Message(payload = "+", isEmpty = true))
    }

    @Test fun test_parse_noParameters() {
        val message = messageParser.parse(IrcMessage(command = "AUTHENTICATE"))

        assertNull(message)
    }

    @Test fun test_serialise_NotEmpty() {
        val message = messageSerialiser.serialise(AuthenticateMessage.Message(payload = "base64payload", isEmpty = false))

        assertEquals(message, IrcMessage(command = "AUTHENTICATE", parameters = listOf("base64payload")))
    }

    @Test fun test_serialise_Empty() {
        val message = messageSerialiser.serialise(AuthenticateMessage.Message(payload = "anotherpayload", isEmpty = true))

        assertEquals(message, IrcMessage(command = "AUTHENTICATE", parameters = listOf("+")))
    }

    // TODO: test Command too
}
