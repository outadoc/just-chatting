package fr.outadoc.justchatting.utils.logging

internal object JvmLogStrategy : LogStrategy {
    override fun println(
        level: Logger.Level,
        tag: String?,
        content: String,
    ) {
        val stream =
            if (level == Logger.Level.Error) {
                System.err
            } else {
                System.out
            }

        stream.println("[${level.tag}] $tag: $content")
    }
}
