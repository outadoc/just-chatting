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

import chat.willow.kale.irc.CharacterCodes

interface ICaseMapper {
    val current: CaseMapping

    fun toLower(string: String): String
    override fun toString(): String
}

enum class CaseMapping(val upperToLowerMapping: Map<Char, Char>) {
    ASCII(emptyMap()),
    STRICT_RFC1459(
        mapOf(
            CharacterCodes.LEFT_STRAIGHT_BRACKET to CharacterCodes.LEFT_CURLY_BRACKET,
            CharacterCodes.RIGHT_STRAIGHT_BRACKET to CharacterCodes.RIGHT_CURLY_BRACKET
        )
    ),
    RFC1459(
        STRICT_RFC1459.upperToLowerMapping.plus(
            mapOf(CharacterCodes.CARET to CharacterCodes.TILDE)
        )
    );

    fun toLower(string: String): String {
        val charArray = string.toCharArray()
        for (i in charArray.indices) {
            val replacement = upperToLowerMapping[charArray[i]]
            if (replacement != null) {
                charArray[i] = replacement
            } else {
                charArray[i] = charArray[i].lowercaseChar()
            }
        }

        return String(charArray)
    }
}

fun equalsIgnoreCase(mapping: CaseMapping, lhs: String, rhs: String): Boolean {
    return mapping.toLower(lhs) == mapping.toLower(rhs)
}
