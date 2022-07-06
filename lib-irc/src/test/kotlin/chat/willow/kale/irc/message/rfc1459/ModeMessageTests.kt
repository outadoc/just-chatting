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

import chat.willow.kale.IKaleParsingStateDelegate
import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.prefix
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ModeMessageTests {

    private lateinit var messageParser: ModeMessage.Message.Parser
    private lateinit var messageSerialiser: ModeMessage.Command.Serialiser
    private lateinit var modeMessage: ModeMessage

    @Before fun setUp() {
        messageParser = ModeMessage.Message.Parser
        messageSerialiser = ModeMessage.Command.Serialiser

        modeMessage = ModeMessage
        modeMessage.parsingStateDelegate = null
    }

    @Test fun test_parse_AddingSingleMode() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#Channel", "+A")))

        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'A')

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#Channel", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_RemovingSingleMode() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#Channel", "-A")))

        val firstModifier = ModeMessage.ModeModifier(type = '-', mode = 'A')

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#Channel", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_AddingMultipleModes_SingleParameter() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#Finnish", "+imI", "*!*@*.fi")))

        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'i')
        val secondModifier = ModeMessage.ModeModifier(type = '+', mode = 'm')
        val thirdModifier = ModeMessage.ModeModifier(type = '+', mode = 'I', parameter = "*!*@*.fi")

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#Finnish", modifiers = listOf(firstModifier, secondModifier, thirdModifier)), message)
    }

    @Test fun test_parse_AddingModesSeparately_SingleParameterEach() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("&oulu", "+b", "*!*@*.edu", "+e", "*!*@*.bu.edu")))

        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'b', parameter = "*!*@*.edu")
        val secondModifier = ModeMessage.ModeModifier(type = '+', mode = 'e', parameter = "*!*@*.bu.edu")

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "&oulu", modifiers = listOf(firstModifier, secondModifier)), message)
    }

    @Test fun test_parse_SettingChannelKeyExample() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#42", "+k", "oulu")))

        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'k', parameter = "oulu")

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#42", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_AddingAndRemovingModes_InOneChunk() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#Channel", "+o-o", "nick1", "nick2")))

        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'o', parameter = "nick1")
        val secondModifier = ModeMessage.ModeModifier(type = '-', mode = 'o', parameter = "nick2")

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#Channel", modifiers = listOf(firstModifier, secondModifier)), message)
    }

    @Test fun test_parse_ListingExceptionMasksExample() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#Channel", "e")))

        val firstModifier = ModeMessage.ModeModifier(mode = 'e')

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#Channel", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_ChannelMode_UsingStateDelegate_ParsesWithParameter() {
        modeMessage.parsingStateDelegate = object : IKaleParsingStateDelegate {
            override fun modeTakesAParameter(isAdding: Boolean, token: Char): Boolean {
                return token == 'x'
            }
        }

        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#Channel", "+x", "a parameter")))

        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'x', parameter = "a parameter")

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#Channel", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_ChannelMode_UsingStateDelegate_ParsesWithoutParameter() {
        modeMessage.parsingStateDelegate = object : IKaleParsingStateDelegate {
            override fun modeTakesAParameter(isAdding: Boolean, token: Char): Boolean {
                return token != 'x'
            }
        }

        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("#Channel", "-x", "extraneous parameter")))

        val firstModifier = ModeMessage.ModeModifier(type = '-', mode = 'x', parameter = null)

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "#Channel", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_UserMode_TurnOffWallOpsExample() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("Wiz", "-w")))

        val firstModifier = ModeMessage.ModeModifier(type = '-', mode = 'w')

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "Wiz", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_UserMode_MakeInvisibleExample() {
        val message = messageParser.parse(IrcMessage(command = "MODE", prefix = "someone", parameters = listOf("Angel", "+i")))

        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'i')

        assertEquals(ModeMessage.Message(source = prefix("someone"), target = "Angel", modifiers = listOf(firstModifier)), message)
    }

    @Test fun test_parse_NoParameters() {
        val message = messageParser.parse(IrcMessage(command = "MODE", parameters = listOf()))

        assertEquals(null, message)
    }

    @Test fun test_parse_OneParameter() {
        val message = messageParser.parse(IrcMessage(command = "MODE", parameters = listOf("parameter")))

        assertEquals(null, message)
    }

    @Test fun test_serialise_AddingSingleMode() {
        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'A')

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "#target", modifiers = listOf(firstModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("#target", "+A")), message)
    }

    @Test fun test_serialise_RemovingSingleMode() {
        val firstModifier = ModeMessage.ModeModifier(type = '-', mode = 'A')

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "#target", modifiers = listOf(firstModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("#target", "-A")), message)
    }

    @Test fun test_serialise_AddingMultipleModes_SingleParameter() {
        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'i')
        val secondModifier = ModeMessage.ModeModifier(type = '+', mode = 'm')
        val thirdModifier = ModeMessage.ModeModifier(type = '+', mode = 'I', parameter = "*!*@*.fi")

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "#Finnish", modifiers = listOf(firstModifier, secondModifier, thirdModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("#Finnish", "+i", "+m", "+I", "*!*@*.fi")), message)
    }

    @Test fun test_serialise_AddingModesSeparately_SingleParameterEach() {
        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'b', parameter = "*!*@*.edu")
        val secondModifier = ModeMessage.ModeModifier(type = '+', mode = 'e', parameter = "*!*@*.bu.edu")

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "&oulu", modifiers = listOf(firstModifier, secondModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("&oulu", "+b", "*!*@*.edu", "+e", "*!*@*.bu.edu")), message)
    }

    @Test fun test_serialise_SettingChannelKeyExample() {
        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'k', parameter = "oulu")

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "#42", modifiers = listOf(firstModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("#42", "+k", "oulu")), message)
    }

    @Test fun test_serialise_AddingAndRemovingModes_InOneChunk() {
        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'o', parameter = "nick1")
        val secondModifier = ModeMessage.ModeModifier(type = '-', mode = 'o', parameter = "nick2")

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "#Channel", modifiers = listOf(firstModifier, secondModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("#Channel", "+o", "nick1", "-o", "nick2")), message)
    }

    @Test fun test_serialise_ListingExceptionMasksExample() {
        val firstModifier = ModeMessage.ModeModifier(mode = 'e')

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "#Channel", modifiers = listOf(firstModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("#Channel", "e")), message)
    }

    @Test fun test_serialise_UserMode_TurnOffWallOpsExample() {
        val firstModifier = ModeMessage.ModeModifier(type = '-', mode = 'w')

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "Wiz", modifiers = listOf(firstModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("Wiz", "-w")), message)
    }

    @Test fun test_serialise_UserMode_MakeInvisibleExample() {
        val firstModifier = ModeMessage.ModeModifier(type = '+', mode = 'i')

        val message = messageSerialiser.serialise(ModeMessage.Command(target = "Angel", modifiers = listOf(firstModifier)))

        assertEquals(IrcMessage(command = "MODE", parameters = listOf("Angel", "+i")), message)
    }
}
