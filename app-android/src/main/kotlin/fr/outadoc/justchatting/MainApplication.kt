package fr.outadoc.justchatting

import android.app.Application
import fr.outadoc.justchatting.utils.logging.AndroidLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.ENABLE_LOGGING) {
            Logger.logStrategy = AndroidLogStrategy
        }
    }
}
