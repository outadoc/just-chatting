package fr.outadoc.justchatting.utils.logging

internal object NoopLogStrategy : LogStrategy {
    override fun println(level: Logger.Level, tag: String?, content: String) = Unit
}
