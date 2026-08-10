package fr.outadoc.justchatting.utils.logging

/**
 * Forwards every log line to each of [strategies], in order. Lets platform entry points
 * combine platform-native output (logcat, stdout, os_log) with [FileLogStrategy].
 */
public class CompositeLogStrategy(
    private val strategies: List<LogStrategy>,
) : LogStrategy {
    override fun println(
        level: Logger.Level,
        tag: String?,
        content: String,
    ) {
        strategies.forEach { strategy -> strategy.println(level, tag, content) }
    }
}
