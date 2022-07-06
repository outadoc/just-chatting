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
package chat.willow.kale.irc.message.extension.cap

import chat.willow.kale.core.message.IrcMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CapEndMessageTests {

    lateinit var messageParser: CapMessage.End.Command.Parser
    lateinit var messageSerialiser: CapMessage.End.Command.Serialiser

    @Before fun setUp() {
        messageParser = CapMessage.End.Command.Parser
        messageSerialiser = CapMessage.End.Command.Serialiser
    }

    @Test fun test_parse() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("END")))

        assertEquals(CapMessage.End.Command, message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf()))

        assertNull(messageOne)
    }

    @Test fun test_serialise() {
        val message = messageSerialiser.serialise(CapMessage.End.Command)

        assertEquals(IrcMessage(command = "CAP", parameters = listOf("END")), message)
    }
}
