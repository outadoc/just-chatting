package fr.outadoc.justchatting

import android.app.Application
import fr.outadoc.justchatting.di.AndroidLogStrategyProvider
import fr.outadoc.justchatting.utils.logging.AndroidLogStrategy
import fr.outadoc.justchatting.utils.logging.CompositeLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Logs are always persisted to a file so they can be shared for support purposes,
        // regardless of whether logcat output is enabled for this build.
        val fileLogStrategy = AndroidLogStrategyProvider.getLogStrategy()

        Logger.logStrategy = if (BuildConfig.ENABLE_LOGGING) {
            CompositeLogStrategy(listOf(AndroidLogStrategy, fileLogStrategy))
        } else {
            fileLogStrategy
        }
    }
}
