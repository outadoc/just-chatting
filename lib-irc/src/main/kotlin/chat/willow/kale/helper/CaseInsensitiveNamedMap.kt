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

open class CaseInsensitiveNamedMap<NamedType : INamed>(private val mapper: ICaseMapper) {

    private val namedThings = mutableMapOf<String, NamedType>()

    val all: Map<String, NamedType>
        get() = namedThings

    fun put(value: NamedType) {
        namedThings[mapper.toLower(value.name)] = value
    }

    fun remove(key: String): NamedType? {
        return namedThings.remove(mapper.toLower(key))
    }

    fun contains(key: String): Boolean {
        return namedThings.contains(mapper.toLower(key))
    }

    operator fun get(key: String): NamedType? {
        return namedThings[mapper.toLower(key)]
    }

    operator fun plusAssign(namedThing: NamedType) {
        put(namedThing)
    }

    operator fun plusAssign(namedThings: Collection<NamedType>) {
        for (namedThing in namedThings) {
            put(namedThing)
        }
    }

    operator fun minusAssign(key: String) {
        remove(key)
    }

    fun clear() {
        namedThings.clear()
    }

    override fun toString(): String {
        return "CaseInsensitiveNamedMap(namedThings=$namedThings,case=${mapper.current})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as CaseInsensitiveNamedMap<*>

        if (namedThings != other.namedThings) return false

        return true
    }

    override fun hashCode(): Int {
        return namedThings.hashCode()
    }
}
