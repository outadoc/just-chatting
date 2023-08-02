package fr.outadoc.justchatting.di

import android.content.Context
import androidx.startup.Initializer
import fr.outadoc.justchatting.component.chatapi.db.inject.dbModule
import fr.outadoc.justchatting.utils.logging.logDebug
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

@Suppress("unused")
class KoinInitializer : Initializer<KoinApplication> {

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    override fun create(context: Context): KoinApplication {
        logDebug<KoinInitializer> { "Setting up Koin dependency injection" }

        return startKoin {
            androidLogger()
            androidContext(context)
            modules(
                mainModule,
                chatModule,
                dbModule,
                homeModule,
                settingsModule,
                mainNavigationModule,
                twitchModule,
            )
        }
    }
}
