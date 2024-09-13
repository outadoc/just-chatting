package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.utils.logging.logDebug
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

public fun startSharedKoin(
    extraBuilder: (KoinApplication.() -> Unit) = {},
): KoinApplication {
    logDebug<KoinApplication> { "Setting up Koin modules" }
    return startKoin {
        extraBuilder()
        modules(
            sharedModule,
            platformModule,
        )
    }
}
