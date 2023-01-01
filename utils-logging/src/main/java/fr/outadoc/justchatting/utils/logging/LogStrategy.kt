package fr.outadoc.justchatting.utils.logging

interface LogStrategy {
    fun println(level: Int, tag: String?, content: String)
}
