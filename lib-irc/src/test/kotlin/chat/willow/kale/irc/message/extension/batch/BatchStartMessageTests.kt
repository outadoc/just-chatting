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
package chat.willow.kale.irc.message.extension.batch

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.prefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BatchStartMessageTests {

    private lateinit var messageParser: BatchMessage.Start.Message.Parser
    private lateinit var messageSerialiser: BatchMessage.Start.Message.Serialiser

    @Before fun setUp() {
        messageParser = BatchMessage.Start.Message.Parser
        messageSerialiser = BatchMessage.Start.Message.Serialiser
    }

    @Test fun test_parse_NoBatchParameters() {
        val message = messageParser.parse(IrcMessage(command = "BATCH", prefix = "someone", parameters = listOf("+batch1", "type")))

        assertEquals(BatchMessage.Start.Message(source = prefix("someone"), reference = "batch1", type = "type"), message)
    }

    @Test fun test_parse_MultipleBatchParameters() {
        val message = messageParser.parse(IrcMessage(command = "BATCH", prefix = "someone", parameters = listOf("+batch1", "type", "param1", "param2")))

        assertEquals(BatchMessage.Start.Message(source = prefix("someone"), reference = "batch1", type = "type", parameters = listOf("param1", "param2")), message)
    }

    @Test fun test_parse_MissingPlusCharacter_ReturnsNull() {
        val message = messageParser.parse(IrcMessage(command = "BATCH", parameters = listOf("batch1", "type")))

        assertNull(message)
    }

    @Test fun test_parse_TooFewParameters_ReturnsNull() {
        val messageOne = messageParser.parse(IrcMessage(command = "BATCH", parameters = listOf("batch1")))
        val messageTwo = messageParser.parse(IrcMessage(command = "BATCH", parameters = listOf()))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_serialise_NoBatchParameters() {
        val message = messageSerialiser.serialise(BatchMessage.Start.Message(source = prefix("someone"), reference = "reference", type = "type"))

        assertEquals(IrcMessage(command = "BATCH", prefix = "someone", parameters = listOf("+reference", "type")), message)
    }

    @Test fun test_serialise_MultipleBatchParameters() {
        val message = messageSerialiser.serialise(BatchMessage.Start.Message(source = prefix("someone"), reference = "reference", type = "type", parameters = listOf("parameter1", "parameter2")))

        assertEquals(IrcMessage(command = "BATCH", prefix = "someone", parameters = listOf("+reference", "type", "parameter1", "parameter2")), message)
    }
}
