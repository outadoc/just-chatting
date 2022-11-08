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

import chat.willow.kale.core.tag.TagStore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TagStoreTests {

    private lateinit var sut: TagStore

    @Before fun setUp() {
        sut = TagStore()
    }

    @Test fun test_get_javaClass() {
        sut.store(Integer(1234))

        val value = sut[Integer::class.java]

        assertEquals(1234, value)
    }

    @Test fun test_get_kotlinClass() {
        sut.store(Integer(5678))

        val value = sut[Integer::class]

        assertEquals(5678, value)
    }

    @Test fun test_store_MultipleThings() {
        sut.store(Integer(9))
        sut.store(java.lang.Boolean(true))

        val firstValue = sut[Integer::class]
        val secondValue = sut[java.lang.Boolean::class]

        assertEquals(9, firstValue)
        assertEquals(true, secondValue)
    }

    @Test fun test_store_SameType_OverwritesPreviousValue() {
        sut.store(Integer(1))
        sut.store(Integer(2))

        val value = sut[Integer::class]

        assertEquals(2, value)
    }
}
