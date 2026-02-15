package fr.outadoc.justchatting.di

import android.content.Context
import androidx.startup.Initializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

@Suppress("unused")
public class KoinInitializer : Initializer<KoinApplication> {
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    override fun create(context: Context): KoinApplication = startSharedKoin {
        androidContext(context)
    }
}
