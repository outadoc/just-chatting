/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.component.ircparser.irc.prefix

internal interface IPrefixParser {
    fun parse(rawPrefix: String): Prefix?
}
