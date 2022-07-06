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

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.core.tag.IKaleTagRouter
import chat.willow.kale.core.tag.ITagParser
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class KaleMetadataFactoryTests {

    private lateinit var sut: KaleMetadataFactory
    private lateinit var tagRouter: IKaleTagRouter

    @Before fun setUp() {
        tagRouter = mock()

        sut = KaleMetadataFactory(tagRouter)
    }

    @Test fun test_process_ValidMetadata_MetadataIsCorrect() {
        val tagParser: ITagParser<String> = mock()
        val metadata = "some metadata"
        whenever(tagParser.parse(any())).thenReturn(metadata)
        whenever(tagRouter.parserFor("tag1")).thenReturn(tagParser)

        val result = sut.construct(IrcMessage(tags = mapOf("tag1" to "value1", "tag2" to null), command = ""))

        assertEquals(metadata, result[String::class])
    }

    @Test fun test_process_InvalidMetadataLookup_MetadataIsNull() {
        val tagParser: ITagParser<String> = mock()
        val metadata = "some metadata"
        whenever(tagParser.parse(any())).thenReturn(metadata)
        whenever(tagRouter.parserFor("tag1")).thenReturn(tagParser)

        val result = sut.construct(IrcMessage(tags = mapOf("tag1" to "value1", "tag2" to null), command = ""))

        assertNull(result[Int::class])
    }
}
