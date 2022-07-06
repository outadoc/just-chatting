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
package chat.willow.kale.core.tag.extension

import chat.willow.kale.core.tag.ITagParser
import chat.willow.kale.core.tag.ITagSerialiser
import chat.willow.kale.core.tag.Tag
import chat.willow.kale.loggerFor
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

data class ServerTimeTag(val time: Instant) {

    companion object Factory : ITagParser<ServerTimeTag>, ITagSerialiser<ServerTimeTag> {

        private val LOGGER = loggerFor<Factory>()

        private val timeZone = TimeZone.getTimeZone("UTC")
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.sss'Z'")

        val name = "time"

        init {
            dateFormat.timeZone = timeZone
        }

        override fun parse(tag: Tag): ServerTimeTag? {
            val value = tag.value ?: return null

            val instant = try {
                Instant.parse(value)
            } catch (exception: Exception) {
                LOGGER.warn("failed to parse date for tag: $tag $exception")

                null
            } ?: return null

            return ServerTimeTag(time = instant)
        }

        override fun serialise(tag: ServerTimeTag): Tag? {
            val instant = tag.time.toString()

            return Tag(name = name, value = instant)
        }
    }
}
