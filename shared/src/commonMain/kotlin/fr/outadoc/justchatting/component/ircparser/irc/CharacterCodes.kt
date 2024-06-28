/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.component.ircparser.irc

internal object CharacterCodes {
    val LF: Char = 0xA.toChar()
    val CR: Char = 0xD.toChar()
    val AT: Char = 0x40.toChar()
    val SPACE: Char = 0x20.toChar()
    val EXCLAM: Char = 0x21.toChar()
    val COLON: Char = 0x3A.toChar()
    val SEMICOLON: Char = 0x3B.toChar()
    val EQUALS: Char = 0x3D.toChar()
    val BACKSLASH: Char = 0x5C.toChar()
}
