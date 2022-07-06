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
package chat.willow.kale.irc

object CharacterCodes {
    val LF: Char = 0xA.toChar()
    val CR: Char = 0xD.toChar()
    val AT: Char = 0x40.toChar()
    val SPACE: Char = 0x20.toChar()
    val EXCLAM: Char = 0x21.toChar()
    @Suppress("UNUSED") val LEFT_ROUNDED_BRACKET: Char = 0x28.toChar()
    @Suppress("UNUSED") val RIGHT_ROUNDED_BRACKET: Char = 0x29.toChar()
    val PLUS: Char = 0x2B.toChar()
    val COMMA: Char = 0x2C.toChar()
    val MINUS: Char = 0x2D.toChar()
    val COLON: Char = 0x3A.toChar()
    val SEMICOLON: Char = 0x3B.toChar()
    val EQUALS: Char = 0x3D.toChar()
    val BACKSLASH: Char = 0x5C.toChar()
    val LEFT_STRAIGHT_BRACKET: Char = 0x5B.toChar()
    val RIGHT_STRAIGHT_BRACKET: Char = 0x5D.toChar()
    val CARET: Char = 0x5E.toChar()
    val LEFT_CURLY_BRACKET: Char = 0x7B.toChar()
    val RIGHT_CURLY_BRACKET: Char = 0x7D.toChar()
    val TILDE: Char = 0x7E.toChar()

    @Suppress("UNUSED") val CTCP = '\u0001'
}
