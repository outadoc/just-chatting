/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.component.ircparser.irc.prefix

internal fun prefix(nick: String) = Prefix(nick = nick)

internal data class Prefix(val nick: String, val user: String? = null, val host: String? = null)
