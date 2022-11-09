package fr.outadoc.justchatting.log

object NoopLogStrategy : LogStrategy {
    override fun println(level: Int, tag: String?, content: String) = Unit
}
