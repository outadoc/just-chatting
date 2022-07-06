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

class CapLsMessageTests {

    lateinit var messageParser: CapMessage.Ls.Message.Parser
    lateinit var messageSerialiser: CapMessage.Ls.Command.Serialiser

    @Before fun setUp() {
        messageParser = CapMessage.Ls.Message.Parser
        messageSerialiser = CapMessage.Ls.Command.Serialiser
    }

    @Test fun test_parse_SingleCap() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "LS", "cap1 ")))

        assertEquals(CapMessage.Ls.Message(source = null, target = "test-nick", caps = mapOf("cap1" to null)), message)
    }

    @Test fun test_parse_MultipleCaps() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "LS", "cap1 cap2=value cap3=")))

        assertEquals(CapMessage.Ls.Message(source = null, target = "test-nick", caps = mapOf("cap1" to null, "cap2" to "value", "cap3" to "")), message)
    }

    @Test fun test_parse_MultilineCaps() {
        val message = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "LS", "*", "cap1 cap2=value cap3=")))

        assertEquals(CapMessage.Ls.Message(source = null, target = "test-nick", caps = mapOf("cap1" to null, "cap2" to "value", "cap3" to ""), isMultiline = true), message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick")))
        val messageThree = messageParser.parse(IrcMessage(command = "CAP", parameters = listOf("test-nick", "LS")))

        assertNull(messageOne)
        assertNull(messageTwo)
        assertNull(messageThree)
    }

    @Test fun test_serialise_Version() {
        val message = messageSerialiser.serialise(CapMessage.Ls.Command(version = "302"))

        assertEquals(IrcMessage(command = "CAP", parameters = listOf("LS", "302")), message)
    }

    @Test fun test_serialise_NoVersion() {
        val message = messageSerialiser.serialise(CapMessage.Ls.Command(version = null))

        assertEquals(IrcMessage(command = "CAP", parameters = listOf("LS")), message)
    }

    // TODO: Command tests too
}
