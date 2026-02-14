/*
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.feature.chat.data.irc.parser.irc

internal object CharacterCodes {
    const val LF: Char = 0xA.toChar()
    const val CR: Char = 0xD.toChar()
    const val AT: Char = 0x40.toChar()
    const val SPACE: Char = 0x20.toChar()
    const val EXCLAM: Char = 0x21.toChar()
    const val COLON: Char = 0x3A.toChar()
    const val SEMICOLON: Char = 0x3B.toChar()
    const val EQUALS: Char = 0x3D.toChar()
    const val BACKSLASH: Char = 0x5C.toChar()
}
