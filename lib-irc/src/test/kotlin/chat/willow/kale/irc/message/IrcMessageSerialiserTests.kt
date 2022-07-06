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
package chat.willow.kale.irc.message

import chat.willow.kale.core.message.IrcMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class IrcMessageSerialiserTests {

    // Properly formed test cases

    @Test fun test_singleCommand() {
        val message = serialiseMessage(IrcMessage(command = "TEST"))

        assertEquals(message, "TEST")
    }

    @Test fun test_prefixAndCommand() {
        val message = serialiseMessage(IrcMessage(prefix = "user!host@server", command = "TEST"))

        assertEquals(message, ":user!host@server TEST")
    }

    @Test fun test_prefixCommandAndParameters() {
        val message = serialiseMessage(IrcMessage(prefix = "user!host@server", command = "TEST", parameters = listOf("some", "parameters", "And some trailing parameters! ")))

        assertEquals(message, ":user!host@server TEST some parameters :And some trailing parameters! ")
    }

    @Test fun test_commandAndTrailingParameters() {
        val message = serialiseMessage(IrcMessage(command = "TEST", parameters = listOf("Trailing parameters")))

        assertEquals(message, "TEST :Trailing parameters")
    }

    @Test fun test_trailingWhitespacePreserved() {
        val message = serialiseMessage(IrcMessage(command = "TEST", parameters = listOf("Trailing parameters with whitespace    ")))

        assertEquals(message, "TEST :Trailing parameters with whitespace    ")
    }

    @Test fun test_tags_specExample() {
        val message = serialiseMessage(IrcMessage(tags = mapOf("aaa" to "bbb", "ccc" to null, "example.com/ddd" to "eee"), prefix = "nick!ident@host.com", command = "PRIVMSG", parameters = listOf("me", "Hello")))

        assertEquals(message, "@aaa=bbb;ccc;example.com/ddd=eee :nick!ident@host.com PRIVMSG me :Hello")
    }

    @Test fun test_tags_singleTagNoValue() {
        val message = serialiseMessage(IrcMessage(tags = mapOf("test" to null), command = "TEST"))

        assertEquals(message, "@test TEST")
    }

    @Test fun test_tag_EscapesValues() {
        val message = serialiseMessage(IrcMessage(tags = mapOf("key" to "; \\\r\nabc"), command = "TEST"))

        assertEquals("@key=\\:\\s\\\\\\r\\nabc TEST", message)
    }

    // Helper functions

    private fun serialiseMessage(message: IrcMessage): String? {
        return IrcMessageSerialiser.serialise(message)
    }
}
