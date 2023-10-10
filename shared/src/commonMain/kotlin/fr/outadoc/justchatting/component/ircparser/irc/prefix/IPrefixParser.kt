package fr.outadoc.justchatting.component.ircparser.irc.prefix

interface IPrefixParser {
    fun parse(rawPrefix: String): Prefix?
}
