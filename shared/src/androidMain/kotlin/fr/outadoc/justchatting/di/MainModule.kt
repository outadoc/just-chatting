package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import fr.outadoc.justchatting.component.chatapi.domain.repository.AuthRepository
import fr.outadoc.justchatting.component.deeplink.DeeplinkParser
import fr.outadoc.justchatting.utils.core.AndroidNetworkStateObserver
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.http.AndroidHttpClientProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import fr.outadoc.justchatting.utils.http.TwitchHttpClientProvider
import kotlinx.datetime.Clock
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

public val mainModule: Module = module {
    single<Clock> { Clock.System }
    single<ConnectivityManager> { get<Context>().getSystemService()!! }
    single<NetworkStateObserver> { AndroidNetworkStateObserver(get()) }
    single { AuthRepository(get(), get(), get()) }
    single { DeeplinkParser(get()) }

    single<BaseHttpClientProvider> { AndroidHttpClientProvider(get()) }
    single<TwitchHttpClientProvider> { TwitchHttpClientProvider(get(), get(), get()) }
    single { get<BaseHttpClientProvider>().get() }
    single(named("twitch")) { get<TwitchHttpClientProvider>().get() }
}
