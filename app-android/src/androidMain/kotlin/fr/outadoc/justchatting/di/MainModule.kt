package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import fr.outadoc.justchatting.component.chatapi.domain.repository.AuthRepository
import fr.outadoc.justchatting.component.deeplink.DeeplinkParser
import fr.outadoc.justchatting.utils.core.AndroidNetworkStateObserver
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.http.HttpClientProvider
import kotlinx.datetime.Clock
import org.koin.core.qualifier.named
import org.koin.dsl.module

val mainModule = module {
    single<Clock> { Clock.System }
    single<ConnectivityManager> { get<Context>().getSystemService()!! }
    single<NetworkStateObserver> { AndroidNetworkStateObserver(get()) }
    single { AuthRepository(get(), get(), get()) }
    single { DeeplinkParser(get()) }

    single { HttpClientProvider(get(), get(), get()) }
    single { get<HttpClientProvider>().getBaseClient() }
    single(qualifier = named("twitch")) {
        get<HttpClientProvider>().getTwitchClient()
    }
}
