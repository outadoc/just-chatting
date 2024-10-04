package fr.outadoc.justchatting.utils.coil

import fr.outadoc.justchatting.utils.logging.Logger

internal class CoilCustomLogger(
    override var minLevel: coil3.util.Logger.Level = coil3.util.Logger.Level.Debug,
) : coil3.util.Logger {

    override fun log(
        tag: String,
        level: coil3.util.Logger.Level,
        message: String?,
        throwable: Throwable?,
    ) {
        Logger.println(
            level = when (level) {
                coil3.util.Logger.Level.Error -> Logger.Level.Error
                coil3.util.Logger.Level.Warn -> Logger.Level.Warning
                coil3.util.Logger.Level.Info -> Logger.Level.Info
                coil3.util.Logger.Level.Debug -> Logger.Level.Debug
                coil3.util.Logger.Level.Verbose -> Logger.Level.Verbose
            },
            tag = tag,
            content = {
                buildString {
                    if (message != null) {
                        appendLine(message)
                    }

                    if (throwable != null) {
                        appendLine(throwable.stackTraceToString())
                    }
                }
            },
        )
    }
}
