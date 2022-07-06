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
package chat.willow.kale.irc.message.utility

import chat.willow.kale.core.message.IrcMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RawMessageTests {

    private lateinit var messageSerialiser: RawMessage.Line.Serialiser

    @Before fun setUp() {
        messageSerialiser = RawMessage.Line.Serialiser
    }

    @Test fun test_serialise_WellFormedLine() {
        val message = messageSerialiser.serialise(RawMessage.Line(line = ":prefix 123 1 :2 3"))

        assertEquals(IrcMessage(command = "123", prefix = "prefix", parameters = listOf("1", "2 3")), message)
    }

    @Test fun test_serialise_BadlyFormedLine_Empty() {
        val message = messageSerialiser.serialise(RawMessage.Line(line = ""))

        assertNull(message)
    }

    @Test fun test_serialise_BadlyFormedLine_Garbage() {
        val message = messageSerialiser.serialise(RawMessage.Line(line = ": :1 :2 :3"))

        assertNull(message)
    }
}
