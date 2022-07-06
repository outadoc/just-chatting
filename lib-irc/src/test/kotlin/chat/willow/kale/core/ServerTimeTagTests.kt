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
package chat.willow.kale.core

import chat.willow.kale.core.tag.Tag
import chat.willow.kale.core.tag.extension.ServerTimeTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ServerTimeTagTests {

    private lateinit var sut: ServerTimeTag.Factory

    @Before fun setUp() {
        sut = ServerTimeTag.Factory
    }

    @Test fun test_parse_SanityCheck() {
        val rawTag = Tag(name = "time", value = "2017-02-19T20:43:12.345Z")

        val tag = sut.parse(rawTag)

        assertEquals(tag, ServerTimeTag(Instant.ofEpochMilli(1487536992345)))
    }

    @Test fun test_parse_MissingValue_ReturnsNull() {
        val rawTag = Tag(name = "time", value = null)

        val tag = sut.parse(rawTag)

        assertNull(tag)
    }

    @Test fun test_parse_UnparseableDate_ReturnsNull() {
        val rawTag = Tag(name = "time", value = "not a time")

        val tag = sut.parse(rawTag)

        assertNull(tag)
    }

    @Test fun test_serialise_SanityCheck() {
        val tag = ServerTimeTag(time = Instant.ofEpochMilli(1487536992345))

        val rawTag = sut.serialise(tag)

        assertEquals(Tag(name = "time", value = "2017-02-19T20:43:12.345Z"), rawTag)
    }
}
