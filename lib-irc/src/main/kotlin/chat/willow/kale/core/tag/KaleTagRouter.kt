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

import chat.willow.kale.core.tag.extension.AccountTag
import chat.willow.kale.core.tag.extension.ServerTimeTag
import kotlin.reflect.KClass

interface IKaleTagRouter {

    fun <T> routeTagToParser(name: String, parser: ITagParser<T>)
    fun parserFor(name: String): ITagParser<*>?

    fun <T : Any> routeTagToSerialiser(tagClass: KClass<T>, serialiser: ITagSerialiser<T>)
    fun <T : Any> serialiserFor(tagClass: Class<T>): ITagSerialiser<T>?
}

class KaleTagRouter : IKaleTagRouter {

    private val namesToParsers = hashMapOf<String, ITagParser<*>>()
    private val tagsToSerialisers = hashMapOf<Class<*>, ITagSerialiser<*>>()

    override fun <T> routeTagToParser(name: String, parser: ITagParser<T>) {
        namesToParsers[name] = parser
    }

    override fun parserFor(name: String): ITagParser<*>? {
        return namesToParsers[name]
    }

    override fun <T : Any> routeTagToSerialiser(tagClass: KClass<T>, serialiser: ITagSerialiser<T>) {
        tagsToSerialisers[tagClass.java] = serialiser
    }

    override fun <T : Any> serialiserFor(tagClass: Class<T>): ITagSerialiser<T>? {
        @Suppress("UNCHECKED_CAST")
        return tagsToSerialisers[tagClass] as? ITagSerialiser<T>
    }

    fun useDefaults(): KaleTagRouter {
        routeTagToParser(AccountTag.name, AccountTag.Factory)
        routeTagToSerialiser(AccountTag::class, AccountTag.Factory)

        routeTagToParser(ServerTimeTag.name, ServerTimeTag.Factory)
        routeTagToSerialiser(ServerTimeTag::class, ServerTimeTag.Factory)

        return this
    }
}
