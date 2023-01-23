package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.chuckerteam.chucker.api.ChuckerInterceptor
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.component.auth.AuthenticationInterceptor
import fr.outadoc.justchatting.component.deeplink.DeeplinkParser
import fr.outadoc.justchatting.component.chatapi.domain.repository.AuthRepository
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import kotlinx.datetime.Clock
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.TimeUnit

val mainModule = module {
    single<Clock> { Clock.System }
    single<ConnectivityManager> { get<Context>().getSystemService()!! }
    single { NetworkStateObserver(get()) }
    single { AuthRepository(get(), get(), get()) }
    single { AuthenticationInterceptor(get(), get()) }
    single { DeeplinkParser(get()) }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthenticationInterceptor>())
            .addInterceptor(ChuckerInterceptor.Builder(get()).build())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.HEADERS
                        }
                    )
                }
            }
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .cache(
                Cache(
                    directory = File(get<Context>().cacheDir, "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
            .build()
    }
}
