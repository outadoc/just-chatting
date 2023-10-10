package fr.outadoc.justchatting.component.ircparser.irc.prefix

fun prefix(nick: String) = Prefix(nick = nick)

data class Prefix(val nick: String, val user: String? = null, val host: String? = null)
