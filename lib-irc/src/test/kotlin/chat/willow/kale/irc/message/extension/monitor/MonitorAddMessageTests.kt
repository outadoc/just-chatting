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
package chat.willow.kale.irc.message.extension.monitor

import chat.willow.kale.core.message.IrcMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MonitorAddMessageTests {

    private lateinit var messageParser: MonitorMessage.Add.Command.Parser
    private lateinit var messageSerialiser: MonitorMessage.Add.Command.Serialiser

    @Before fun setUp() {
        messageParser = MonitorMessage.Add.Command.Parser
        messageSerialiser = MonitorMessage.Add.Command.Serialiser
    }

    @Test fun test_parse_SingleTarget() {
        val message = messageParser.parse(IrcMessage(command = "MONITOR", parameters = listOf("+", "target")))

        assertEquals(MonitorMessage.Add.Command(targets = listOf("target")), message)
    }

    @Test fun test_parse_MultipleTargets() {
        val message = messageParser.parse(IrcMessage(command = "MONITOR", parameters = listOf("+", "target1,target2")))

        assertEquals(MonitorMessage.Add.Command(targets = listOf("target1", "target2")), message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "MONITOR", parameters = listOf("+")))
        val messageTwo = messageParser.parse(IrcMessage(command = "MONITOR", parameters = listOf()))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_serialise_SingleTarget() {
        val message = messageSerialiser.serialise(MonitorMessage.Add.Command(targets = listOf("target")))

        assertEquals(IrcMessage(command = "MONITOR", parameters = listOf("+", "target")), message)
    }

    @Test fun test_serialise_MultipleTargets() {
        val message = messageSerialiser.serialise(MonitorMessage.Add.Command(targets = listOf("target1", "target2")))

        assertEquals(IrcMessage(command = "MONITOR", parameters = listOf("+", "target1,target2")), message)
    }
}
