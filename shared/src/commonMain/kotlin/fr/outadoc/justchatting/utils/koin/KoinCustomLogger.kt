package fr.outadoc.justchatting.utils.koin

import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

internal object KoinCustomLogger : Logger() {
    override fun display(level: Level, msg: MESSAGE) {
        fr.outadoc.justchatting.utils.logging.Logger.println(
            level = when (level) {
                Level.DEBUG -> fr.outadoc.justchatting.utils.logging.Logger.Level.Debug
                Level.INFO -> fr.outadoc.justchatting.utils.logging.Logger.Level.Info
                Level.WARNING -> fr.outadoc.justchatting.utils.logging.Logger.Level.Warning
                Level.ERROR -> fr.outadoc.justchatting.utils.logging.Logger.Level.Error
                Level.NONE -> fr.outadoc.justchatting.utils.logging.Logger.Level.Verbose
            },
            tag = "Koin",
            content = { msg },
        )
    }
}
