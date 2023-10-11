package fr.outadoc.justchatting.utils.logging

object NoopLogStrategy : LogStrategy {
    override fun println(level: Logger.Level, tag: String?, content: String) = Unit
}
