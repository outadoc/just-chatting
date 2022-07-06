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
package chat.willow.kale.core.tag

import kotlin.reflect.KClass

interface ITagStore {
    operator fun <T : Any>get(classType: KClass<T>): T?
    operator fun <T : Any>get(classType: Class<T>): T?

    fun <T : Any>store(thing: T)
}

data class TagStore(private val store: MutableMap<Class<*>, Any> = mutableMapOf()) : ITagStore {

    override fun <T : Any> get(classType: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return store[classType] as? T
    }

    override fun <T : Any> get(classType: KClass<T>): T? {
        return this[classType.java]
    }

    override fun <T : Any> store(thing: T) {
        store[thing::class.java] = thing
    }

    override fun toString(): String {
        val content = if (store.isEmpty()) {
            "Empty"
        } else {
            "content=$store"
        }

        return "TagStore($content)"
    }
}
