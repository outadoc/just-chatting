package fr.outadoc.justchatting.utils.logging

public object NoopLogStrategy : LogStrategy {
    override fun println(
        level: Logger.Level,
        tag: String?,
        content: String,
    ): Unit = Unit
}
