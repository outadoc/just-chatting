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

class CapNewMessageTests {

    lateinit var messageParser: CapMessage.New.Message.Parser
    lateinit var messageSerialiser: CapMessage.New.Message.Serialiser

    @Before fun setUp() {
        messageParser = CapMessage.New.Message.Parser
        messageSerialiser = CapMessage.New.Message.Serialiser
    }

    @Test fun test_parse_SingleCap() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "NEW", "cap1 ")))

        assertEquals(CapMessage.New.Message(source = null, target = "test-nick", caps = mapOf("cap1" to null)), message)
    }

    @Test fun test_parse_MultipleCaps() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "NEW", "cap1 cap2=value cap3=")))

        assertEquals(CapMessage.New.Message(source = null, target = "test-nick", caps = mapOf("cap1" to null, "cap2" to "value", "cap3" to "")), message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick")))
        val messageThree = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "NEW")))

        assertNull(messageOne)
        assertNull(messageTwo)
        assertNull(messageThree)
    }

    @Test fun test_serialise_SingleCap() {
        val message = messageSerialiser.serialise(CapMessage.New.Message(source = null, target = "someone", caps = mapOf("cap1" to null)))

        assertEquals(IrcMessage(command = "CAP", parameters = listOf("someone", "NEW", "cap1")), message)
    }

    @Test fun test_serialise_MultipleCaps() {
        val message = messageSerialiser.serialise(CapMessage.New.Message(source = null, target = "someone", caps = mapOf("cap1" to null, "cap2" to "", "cap3" to "val3")))

        assertEquals(IrcMessage(command = "CAP", parameters = listOf("someone", "NEW", "cap1 cap2= cap3=val3")), message)
    }
}
