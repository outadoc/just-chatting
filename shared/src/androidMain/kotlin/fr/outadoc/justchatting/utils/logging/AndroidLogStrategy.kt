package fr.outadoc.justchatting.utils.logging

import android.util.Log

public object AndroidLogStrategy : LogStrategy {
    override fun println(
        level: Logger.Level,
        tag: String?,
        content: String,
    ) {
        Log.println(level.toPlatform(), tag, content)
    }

    private fun Logger.Level.toPlatform(): Int = when (this) {
        Logger.Level.Verbose -> Log.VERBOSE
        Logger.Level.Debug -> Log.DEBUG
        Logger.Level.Info -> Log.INFO
        Logger.Level.Warning -> Log.WARN
        Logger.Level.Error -> Log.ERROR
    }
}
