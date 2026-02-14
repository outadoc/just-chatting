package fr.outadoc.justchatting.utils.logging

public interface LogStrategy {
    public fun println(
        level: Logger.Level,
        tag: String?,
        content: String,
    )
}
