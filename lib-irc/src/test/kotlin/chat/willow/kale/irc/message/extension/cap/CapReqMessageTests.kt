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

class CapReqMessageTests {

    lateinit var messageParser: CapMessage.Req.Command.Parser
    lateinit var messageSerialiser: CapMessage.Req.Command.Serialiser

    @Before fun setUp() {
        messageParser = CapMessage.Req.Command.Parser
        messageSerialiser = CapMessage.Req.Command.Serialiser
    }

    @Test fun test_parse_SingleCap() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("REQ", "cap1 ")))

        assertEquals(CapMessage.Req.Command(caps = listOf("cap1")), message)
    }

    @Test fun test_parse_MultipleCaps() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("REQ", "cap1 cap2 cap3")))

        assertEquals(CapMessage.Req.Command(caps = listOf("cap1", "cap2", "cap3")), message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick")))
        val messageThree = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "REQ")))

        assertNull(messageOne)
        assertNull(messageTwo)
        assertNull(messageThree)
    }

    @Test fun test_serialise_SingleCap() {
        val message = messageSerialiser.serialise(CapMessage.Req.Command(caps = listOf("cap1")))

        assertEquals(IrcMessage(command = "CAP", parameters = listOf("REQ", "cap1")), message)
    }

    @Test fun test_serialise_MultipleCaps() {
        val message = messageSerialiser.serialise(CapMessage.Req.Command(caps = listOf("cap1", "cap2", "cap3")))

        assertEquals(IrcMessage(command = "CAP", parameters = listOf("REQ", "cap1 cap2 cap3")), message)
    }
}
