/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.prefix

internal interface IPrefixParser {
    fun parse(rawPrefix: String): Prefix?
}
