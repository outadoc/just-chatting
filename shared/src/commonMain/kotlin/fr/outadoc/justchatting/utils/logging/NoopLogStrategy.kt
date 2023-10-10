package fr.outadoc.justchatting.utils.logging

object NoopLogStrategy : LogStrategy {
    override fun println(level: Int, tag: String?, content: String) = Unit
}
