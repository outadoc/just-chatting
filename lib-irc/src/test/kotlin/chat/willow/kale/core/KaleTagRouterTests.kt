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

import chat.willow.kale.core.tag.ITagParser
import chat.willow.kale.core.tag.ITagSerialiser
import chat.willow.kale.core.tag.KaleTagRouter
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class KaleTagRouterTests {

    private lateinit var sut: KaleTagRouter

    @Before fun setUp() {
        sut = KaleTagRouter()
    }

    @Test fun test_parserFor_AfterRegisteringTagParser_ReturnsCorrectParser() {
        val mockTagParser: ITagParser<*> = mock()
        sut.routeTagToParser("test", mockTagParser)

        val parser = sut.parserFor("test")

        assertSame(mockTagParser, parser)
    }

    @Test fun test_parserFor_WrongName_ReturnsNull() {
        val mockTagParser: ITagParser<*> = mock()
        sut.routeTagToParser("test", mockTagParser)

        val parser = sut.parserFor("different test")

        assertNull(parser)
    }

    @Test fun test_serialiserFor_AfterRegisteringTagSerialiser_ReturnsCorrectSerialiser() {
        val mockTagSerialiser: ITagSerialiser<Any> = mock()
        val mockTag: Any = mock()
        sut.routeTagToSerialiser(mockTag::class, mockTagSerialiser)

        val serialiser = sut.serialiserFor(mockTag::class.java)

        assertSame(mockTagSerialiser, serialiser)
    }

    @Test fun test_serialiserFor_WrongType_ReturnsNull() {
        val mockTagSerialiser: ITagSerialiser<Any> = mock()
        val mockTag: Any = mock()
        sut.routeTagToSerialiser(mockTag::class, mockTagSerialiser)

        val serialiser = sut.serialiserFor(Int::class.java)

        assertNull(serialiser)
    }
}
