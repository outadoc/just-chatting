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
package chat.willow.kale.helper

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CaseInsensitiveNamedMapTests {

    private lateinit var sut: CaseInsensitiveNamedMap<MockNamedThing>
    private lateinit var mockMapper: ICaseMapper

    @Before fun setUp() {
        mockMapper = mock()
        sut = CaseInsensitiveNamedMap(mockMapper)
    }

    @Test fun `get uses mapper`() {
        sut["something"]

        verify(mockMapper).toLower("something")
    }

    @Test fun `put uses mapper`() {
        sut += MockNamedThing(name = "test")

        verify(mockMapper).toLower("test")
    }

    @Test fun `getting named thing with different cases results in correct item`() {
        val namedThing = MockNamedThing()

        sut += namedThing
        val result = sut["a test name"]

        assertTrue(result === namedThing)
    }

    @Test fun `removing named thing with different cases results in correct removal`() {
        val namedThing = MockNamedThing(name = "test name")
        sut += namedThing

        sut -= "TEST NAME"
        val result = sut["test name"]

        assertNull(result)
    }

    @Test fun `clear removes all named things`() {
        val namedThingOne = MockNamedThing(name = "test name 2")
        val namedThingTwo = MockNamedThing(name = "test name 1")
        sut += listOf(namedThingOne, namedThingTwo)

        sut.clear()
        val resultOne = sut["test name 1"]
        val resultTwo = sut["test name 2"]

        assertNull(resultOne)
        assertNull(resultTwo)
    }

    @Test fun `contains returns true for different cased things`() {
        val namedThing = MockNamedThing(name = "test name")
        sut += namedThing

        val result = sut.contains("TEST NAME")

        assertTrue(result)
    }
}

private class MockNamedThing(override val name: String = "test") : INamed
