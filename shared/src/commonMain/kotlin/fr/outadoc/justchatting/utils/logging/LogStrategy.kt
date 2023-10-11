package fr.outadoc.justchatting.utils.logging

interface LogStrategy {
    fun println(level: Logger.Level, tag: String?, content: String)
}
