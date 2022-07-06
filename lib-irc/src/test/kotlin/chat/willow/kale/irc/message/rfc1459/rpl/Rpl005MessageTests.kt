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
package chat.willow.kale.irc.message.rfc1459.rpl

import chat.willow.kale.core.message.IrcMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class Rpl005MessageTests {

    private lateinit var messageParser: Rpl005Message.Message.Parser
    private lateinit var messageSerialiser: Rpl005Message.Message.Serialiser

    @Before fun setUp() {
        messageParser = Rpl005Message.Message.Parser
        messageSerialiser = Rpl005Message.Message.Serialiser
    }

    @Test fun test_parse_SingleToken_NoValue() {
        val message = messageParser.parse(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY")))

        assertEquals(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to null)), message)
    }

    @Test fun test_parse_SingleToken_NoValue_ButEqualsPresent() {
        val message = messageParser.parse(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY=")))

        assertEquals(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to null)), message)
    }

    @Test fun test_parse_SingleToken_WithValue() {
        val message = messageParser.parse(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY=VALUE")))

        assertEquals(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to "VALUE")), message)
    }

    @Test fun test_parse_MultipleTokens_MultipleTypesOfValues() {
        val message = messageParser.parse(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY=VALUE", "KEY2", "KEY3=", "KEY4=\uD83D\uDC30")))

        assertEquals(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to "VALUE", "KEY2" to null, "KEY3" to null, "KEY4" to "\uD83D\uDC30")), message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "005", parameters = listOf()))
        val messageTwo = messageParser.parse(IrcMessage(command = "005", parameters = listOf("test-nickname2")))

        assertNull(messageOne)
        assertNull(messageTwo)
    }

    @Test fun test_serialise_SingleToken_NoValue() {
        val message = messageSerialiser.serialise(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to null)))

        assertEquals(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY")), message)
    }

    @Test fun test_serialise_SingleToken_NoValue_ButEqualsPresent() {
        val message = messageSerialiser.serialise(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to null)))

        assertEquals(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY")), message)
    }

    @Test fun test_serialise_SingleToken_WithValue() {
        val message = messageSerialiser.serialise(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to "VALUE")))

        assertEquals(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY=VALUE")), message)
    }

    @Test fun test_serialise_MultipleTokens_MultipleTypesOfValues() {
        val message = messageSerialiser.serialise(Rpl005Message.Message(source = "imaginary.bunnies.io", target = "test-nickname", tokens = mapOf("KEY" to "VALUE", "KEY2" to null, "KEY3" to null, "KEY4" to "\uD83D\uDC30")))

        assertEquals(IrcMessage(command = "005", prefix = "imaginary.bunnies.io", parameters = listOf("test-nickname", "KEY=VALUE", "KEY2", "KEY3", "KEY4=\uD83D\uDC30")), message)
    }
}
