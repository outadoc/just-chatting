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

class NoticeMessageTests {

    private lateinit var messageParser: NoticeMessage.Message.Parser
    private lateinit var messageSerialiser: NoticeMessage.Command.Serialiser

    @Before fun setUp() {
        messageParser = NoticeMessage.Message.Parser
        messageSerialiser = NoticeMessage.Command.Serialiser
    }

    @Test fun test_parse_MessageFromUser() {
        val message = messageParser.parse(IrcMessage(command = "NOTICE", prefix = "Angel", parameters = listOf("Wiz", "Hello are you receiving this message ?")))

        assertEquals(message, NoticeMessage.Message(source = Prefix(nick = "Angel"), target = "Wiz", message = "Hello are you receiving this message ?"))
    }

    @Test fun test_parse_MessageToUser() {
        val message = messageParser.parse(IrcMessage(command = "NOTICE", prefix = "someone", parameters = listOf("Angel", "yes I'm receiving it !")))

        assertEquals(message, NoticeMessage.Message(source = prefix("someone"), target = "Angel", message = "yes I'm receiving it !"))
    }

    @Test fun test_parse_MessageToHostmask() {
        val message = messageParser.parse(IrcMessage(command = "NOTICE", prefix = "someone", parameters = listOf("jto@tolsun.oulu.fi", "Hello !")))

        assertEquals(message, NoticeMessage.Message(source = prefix("someone"), target = "jto@tolsun.oulu.fi", message = "Hello !"))
    }

    @Test fun test_parse_MessageToServerWildcard() {
        val message = messageParser.parse(IrcMessage(command = "NOTICE", prefix = "someone", parameters = listOf("$*.fi", "Server tolsun.oulu.fi rebooting.")))

        assertEquals(message, NoticeMessage.Message(source = prefix("someone"), target = "$*.fi", message = "Server tolsun.oulu.fi rebooting."))
    }

    @Test fun test_parse_MessageToHostWildcard() {
        val message = messageParser.parse(IrcMessage(command = "NOTICE", prefix = "someone", parameters = listOf("#*.edu", "NSFNet is undergoing work, expect interruptions")))

        assertEquals(message, NoticeMessage.Message(source = prefix("someone"), target = "#*.edu", message = "NSFNet is undergoing work, expect interruptions"))
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "NOTICE", prefix = "someone", parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "NOTICE", prefix = "someone", parameters = listOf("test")))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_parse_NoSource() {
        val message = messageParser.parse(IrcMessage(command = "NOTICE", parameters = listOf("test")))

        assertNull(message)
    }

    @Test fun test_serialise_MessageToUser() {
        val message = messageSerialiser.serialise(NoticeMessage.Command(target = "Angel", message = "yes I'm receiving it !"))

        assertEquals(message, IrcMessage(command = "NOTICE", parameters = listOf("Angel", "yes I'm receiving it !")))
    }

    @Test fun test_serialise_MessageToHostmask() {
        val message = messageSerialiser.serialise(NoticeMessage.Command(target = "jto@tolsun.oulu.fi", message = "Hello !"))

        assertEquals(message, IrcMessage(command = "NOTICE", parameters = listOf("jto@tolsun.oulu.fi", "Hello !")))
    }

    @Test fun test_serialise_MessageToServerWildcard() {
        val message = messageSerialiser.serialise(NoticeMessage.Command(target = "$*.fi", message = "Server tolsun.oulu.fi rebooting."))

        assertEquals(message, IrcMessage(command = "NOTICE", parameters = listOf("$*.fi", "Server tolsun.oulu.fi rebooting.")))
    }

    @Test fun test_serialise_MessageToHostWildcard() {
        val message = messageSerialiser.serialise(NoticeMessage.Command(target = "#*.edu", message = "NSFNet is undergoing work, expect interruptions"))

        assertEquals(message, IrcMessage(command = "NOTICE", parameters = listOf("#*.edu", "NSFNet is undergoing work, expect interruptions")))
    }
}
