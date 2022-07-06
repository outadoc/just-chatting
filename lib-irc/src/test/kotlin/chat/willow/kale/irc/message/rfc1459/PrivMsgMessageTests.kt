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

class PrivMsgMessageTests {

    private lateinit var messageParser: PrivMsgMessage.Message.Parser
    private lateinit var commandParser: PrivMsgMessage.Command.Parser
    private lateinit var commandSerialiser: PrivMsgMessage.Command.Serialiser
    private lateinit var messageSerialiser: PrivMsgMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = PrivMsgMessage.Message.Parser
        commandParser = PrivMsgMessage.Command.Parser
        commandSerialiser = PrivMsgMessage.Command.Serialiser
        messageSerialiser = PrivMsgMessage.Message.Serialiser
    }

    @Test fun test_parse_MessageFromUser() {
        val message = messageParser.parse(IrcMessage(command = "PRIVMSG", prefix = "Angel", parameters = listOf("Wiz", "Hello are you receiving this message ?")))

        assertEquals(message, PrivMsgMessage.Message(source = Prefix(nick = "Angel"), target = "Wiz", message = "Hello are you receiving this message ?"))
    }

    @Test fun test_parse_MessageToUser() {
        val message = messageParser.parse(IrcMessage(command = "PRIVMSG", prefix = "someone", parameters = listOf("Angel", "yes I'm receiving it !")))

        assertEquals(message, PrivMsgMessage.Message(source = prefix("someone"), target = "Angel", message = "yes I'm receiving it !"))
    }

    @Test fun test_parse_MessageToHostmask() {
        val message = messageParser.parse(IrcMessage(command = "PRIVMSG", prefix = "someone", parameters = listOf("jto@tolsun.oulu.fi", "Hello !")))

        assertEquals(message, PrivMsgMessage.Message(source = prefix("someone"), target = "jto@tolsun.oulu.fi", message = "Hello !"))
    }

    @Test fun test_parse_MessageToServerWildcard() {
        val message = messageParser.parse(IrcMessage(command = "PRIVMSG", prefix = "someone", parameters = listOf("$*.fi", "Server tolsun.oulu.fi rebooting.")))

        assertEquals(message, PrivMsgMessage.Message(source = prefix("someone"), target = "$*.fi", message = "Server tolsun.oulu.fi rebooting."))
    }

    @Test fun test_parse_MessageToHostWildcard() {
        val message = messageParser.parse(IrcMessage(command = "PRIVMSG", prefix = "someone", parameters = listOf("#*.edu", "NSFNet is undergoing work, expect interruptions")))

        assertEquals(message, PrivMsgMessage.Message(source = prefix("someone"), target = "#*.edu", message = "NSFNet is undergoing work, expect interruptions"))
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "PRIVMSG", parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "PRIVMSG", parameters = listOf("test")))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_parse_Command() {
        val message = commandParser.parse(IrcMessage(command = "PRIVMSG", parameters = listOf("target", "a message")))

        assertEquals(message, PrivMsgMessage.Command(target = "target", message = "a message"))
    }

    @Test fun test_parse_Command_TooFewParameters() {
        val messageOne = commandParser.parse(IrcMessage(command = "PRIVMSG", parameters = listOf()))
        val messageTwo = commandParser.parse(IrcMessage(command = "PRIVMSG", parameters = listOf("test")))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_serialise_MessageToUser() {
        val message = commandSerialiser.serialise(PrivMsgMessage.Command(target = "Angel", message = "yes I'm receiving it !"))

        assertEquals(message, IrcMessage(command = "PRIVMSG", parameters = listOf("Angel", "yes I'm receiving it !")))
    }

    @Test fun test_serialise_MessageToHostmask() {
        val message = commandSerialiser.serialise(PrivMsgMessage.Command(target = "jto@tolsun.oulu.fi", message = "Hello !"))

        assertEquals(message, IrcMessage(command = "PRIVMSG", parameters = listOf("jto@tolsun.oulu.fi", "Hello !")))
    }

    @Test fun test_serialise_MessageToServerWildcard() {
        val message = commandSerialiser.serialise(PrivMsgMessage.Command(target = "$*.fi", message = "Server tolsun.oulu.fi rebooting."))

        assertEquals(message, IrcMessage(command = "PRIVMSG", parameters = listOf("$*.fi", "Server tolsun.oulu.fi rebooting.")))
    }

    @Test fun test_serialise_MessageToHostWildcard() {
        val message = commandSerialiser.serialise(PrivMsgMessage.Command(target = "#*.edu", message = "NSFNet is undergoing work, expect interruptions"))

        assertEquals(message, IrcMessage(command = "PRIVMSG", parameters = listOf("#*.edu", "NSFNet is undergoing work, expect interruptions")))
    }

    @Test fun test_serialise_Message() {
        val message = messageSerialiser.serialise(PrivMsgMessage.Message(source = prefix("server"), target = "someone", message = "server message"))

        assertEquals(message, IrcMessage(command = "PRIVMSG", prefix = "server", parameters = listOf("someone", "server message")))
    }
}
