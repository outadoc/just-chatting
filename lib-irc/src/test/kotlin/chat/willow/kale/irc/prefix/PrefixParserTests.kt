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
package chat.willow.kale.irc.prefix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PrefixParserTests {
    lateinit var prefixParser: IPrefixParser

    @Before fun setUp() {
        prefixParser = PrefixParser
    }

    @Test fun test_parse_MissingNick() {
        val prefixOne = prefixParser.parse("")
        val prefixTwo = prefixParser.parse("!prefix")
        val prefixThree = prefixParser.parse("@host")
        val prefixFour = prefixParser.parse("!prefix@host")
        val prefixFive = prefixParser.parse("!@host")
        val prefixSix = prefixParser.parse("!prefix@")

        assertNull(prefixOne)
        assertNull(prefixTwo)
        assertNull(prefixThree)
        assertNull(prefixFour)
        assertNull(prefixFive)
        assertNull(prefixSix)
    }

    @Test fun test_parse_NoHost() {
        val prefixOne = prefixParser.parse("nick")
        val prefixTwo = prefixParser.parse("nick!prefix")

        assertEquals(Prefix(nick = "nick"), prefixOne)
        assertEquals(Prefix(nick = "nick", user = "prefix"), prefixTwo)
    }

    @Test fun test_parse_WellFormed() {
        val prefixOne = prefixParser.parse("nick@host")
        val prefixTwo = prefixParser.parse("nick!prefix@host")

        assertEquals(Prefix(nick = "nick", host = "host"), prefixOne)
        assertEquals(Prefix(nick = "nick", user = "prefix", host = "host"), prefixTwo)
    }

    @Test fun test_parse_BlankNonNickFields() {
        val prefixOne = prefixParser.parse("nick!")
        val prefixTwo = prefixParser.parse("nick@")
        val prefixThree = prefixParser.parse("nick!@")

        assertEquals(Prefix(nick = "nick", user = ""), prefixOne)
        assertEquals(Prefix(nick = "nick", host = ""), prefixTwo)
        assertEquals(Prefix(nick = "nick", user = "", host = ""), prefixThree)
    }

    @Test fun test_parse_WeirdExamples() {
        val prefixOne = prefixParser.parse("nick!prefix!resu@host")
        val prefixTwo = prefixParser.parse("nick@kcin!prefix@host")

        assertEquals(Prefix(nick = "nick", user = "prefix!resu", host = "host"), prefixOne)
        assertEquals(Prefix(nick = "nick@kcin", user = "prefix", host = "host"), prefixTwo)
    }
}
