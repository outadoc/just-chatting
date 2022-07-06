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
package chat.willow.kale.irc.message.extension.account_notify

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.prefix.Prefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AccountMessageTests {

    lateinit var messageParser: AccountMessage.Message.Parser
    lateinit var messageSerialiser: AccountMessage.Message.Serialiser

    @Before fun setUp() {
        messageParser = AccountMessage.Message.Parser
        messageSerialiser = AccountMessage.Message.Serialiser
    }

    @Test fun test_parse_SourceAndAccount() {
        val message = messageParser.parse(IrcMessage(command = "ACCOUNT", prefix = "nickname", parameters = listOf("account")))

        assertEquals(AccountMessage.Message(source = Prefix(nick = "nickname"), account = "account"), message)
    }

    @Test fun test_parse_MissingPrefix_ReturnsNull() {
        val message = messageParser.parse(IrcMessage(command = "ACCOUNT", parameters = listOf("account")))

        assertNull(message)
    }

    @Test fun test_parse_TooFewParameters() {
        val messageOne = messageParser.parse(IrcMessage(command = "ACCOUNT", prefix = "nickname"))

        assertNull(messageOne)
    }

    @Test fun test_serialise_Source_Account() {
        val message = messageSerialiser.serialise(AccountMessage.Message(source = Prefix(nick = "nickname"), account = "account"))

        assertEquals(IrcMessage(command = "ACCOUNT", prefix = "nickname", parameters = listOf("account")), message)
    }
}
