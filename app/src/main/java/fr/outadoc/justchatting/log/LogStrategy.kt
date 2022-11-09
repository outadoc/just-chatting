package fr.outadoc.justchatting.log

interface LogStrategy {
    fun println(level: Int, tag: String?, content: String)
}
