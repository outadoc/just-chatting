/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.component.ircparser.irc.prefix

import fr.outadoc.justchatting.component.ircparser.irc.CharacterCodes

object PrefixParser : IPrefixParser {

    override fun parse(rawPrefix: String): Prefix? {
        var raw = rawPrefix

        if (raw.isEmpty()) {
            return null
        }

        var nick = raw
        var user: String? = null
        var host: String? = null

        val indexOfLastAt = rawPrefix.indexOfLast { character -> character == CharacterCodes.AT }
        if (indexOfLastAt >= 0) {
            raw = rawPrefix.substring(0, indexOfLastAt)
            nick = raw

            host = if (rawPrefix.length > indexOfLastAt + 2) {
                rawPrefix.substring(indexOfLastAt + 1, rawPrefix.length)
            } else {
                ""
            }
        }

        val indexOfFirstExclam = raw.indexOfFirst { character -> character == CharacterCodes.EXCLAM }
        if (indexOfFirstExclam >= 0) {
            nick = raw.substring(0, indexOfFirstExclam)

            user = if (raw.length > indexOfFirstExclam + 2) {
                raw.substring(indexOfFirstExclam + 1, raw.length)
            } else {
                ""
            }
        }

        if (nick.isEmpty()) {
            return null
        }

        return Prefix(nick = nick, user = user, host = host)
    }
}
