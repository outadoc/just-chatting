package fr.outadoc.justchatting.di

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

@Suppress("unused")
class KoinInitializer : Initializer<KoinApplication> {

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    override fun create(context: Context): KoinApplication {
        Log.d("KoinInitializer", "Setting up Koin dependency injection")

        return startKoin {
            androidLogger()
            androidContext(context)
            modules(mainModule, viewModelModule)
        }
    }
}
