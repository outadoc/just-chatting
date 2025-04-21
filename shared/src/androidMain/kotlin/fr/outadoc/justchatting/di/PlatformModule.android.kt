package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.auth.data.AuthCallbackWebServer
import fr.outadoc.justchatting.feature.auth.data.NoopAuthCallbackWebServer
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.mobile.AndroidChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.mobile.AndroidCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.preferences.presentation.AndroidLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AndroidAppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppVersionNameProvider
import fr.outadoc.justchatting.utils.http.AndroidHttpClientProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module
    get() = module {
        single {
            OAuthAppCredentials(
                clientId = "l9klwmh97qgn0s0me276ezsft5szp2",
                redirectUri = "https://just-chatting.app/auth/callback.html",
            )
        }

        single<ChatNotifier> { AndroidChatNotifier(get(), get()) }
        single<CreateShortcutForChannelUseCase> { AndroidCreateShortcutForChannelUseCase(get()) }

        single<SqlDriver> {
            AndroidSqliteDriver(
                schema = AppDatabase.Schema,
                context = get(),
                name = "database",
            )
        }

        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    get<Context>().filesDir.absolutePath.toPath()
                        .resolve("datastore")
                        .resolve("fr.outadoc.justchatting.preferences_pb")
                },
            )
        }

        single<ConnectivityManager> { get<Context>().getSystemService()!! }
        single<BaseHttpClientProvider> { AndroidHttpClientProvider(get(), get()) }

        single<LogRepository> { AndroidLogRepository(get()) }
        single<AppVersionNameProvider> { AndroidAppVersionNameProvider(get()) }
        single<AuthCallbackWebServer> { NoopAuthCallbackWebServer() }
    }
