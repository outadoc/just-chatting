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
package chat.willow.kale

import chat.willow.kale.core.message.IMessageSerialiser
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KaleRouterTests {

    private lateinit var sut: KaleRouter

    @Before fun setUp() {
        sut = KaleRouter()
    }

    @Test fun test_serialiserFor_MessageNotRegistered_ReturnsNull() {
        val serialiser = sut.serialiserFor(Int::class.java)

        assertNull(serialiser)
    }

    @Test fun test_serialiserFor_MessageRegistered_ReturnsCorrectSerialiser() {
        val serialiserOne: IMessageSerialiser<Int> = mock()
        sut.register(Int::class, serialiserOne)

        val serialiser = sut.serialiserFor(Int::class.java)

        assertTrue(serialiserOne === serialiser)
    }

    @Test fun test_serialiserFor_DifferentMessageRegistered_ReturnsCorrectSerialiser() {
        val serialiserOne: IMessageSerialiser<Int> = mock()
        val serialiserTwo: IMessageSerialiser<String> = mock()
        val newSerialiserOne: IMessageSerialiser<Int> = mock()
        sut.register(Int::class, serialiserOne)
        sut.register(String::class, serialiserTwo)
        sut.register(Int::class, newSerialiserOne)

        val serialiser = sut.serialiserFor(Int::class.java)

        assertTrue(newSerialiserOne === serialiser)
    }
}
