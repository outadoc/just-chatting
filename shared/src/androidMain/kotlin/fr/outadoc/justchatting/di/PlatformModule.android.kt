package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.jordond.connectivity.Connectivity
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.auth.data.LocalCallbackWebServer
import fr.outadoc.justchatting.feature.auth.data.NoopLocalCallbackWebServer
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.preferences.presentation.AndroidAppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.AndroidLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.AppUpdateChecker
import fr.outadoc.justchatting.feature.preferences.presentation.AppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.NoopAppUpdateChecker
import fr.outadoc.justchatting.utils.http.AndroidHttpClientProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import fr.outadoc.justchatting.utils.logging.FileLogStrategy
import fr.outadoc.justchatting.utils.logging.LogStrategy
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal actual val platformModule: Module
    get() =
        module {
            single {
                OAuthAppCredentials(
                    clientId = "l9klwmh97qgn0s0me276ezsft5szp2",
                    redirectUri = "https://just-chatting.app/auth/callback.html",
                )
            }

            single<Path>(named("logsDirectory")) { get<Context>().cacheDir.toOkioPath() / "logs" }
            single<Path>(named("logFilePath")) { get<Path>(named("logsDirectory")) / "app.log" }

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
                        get<Context>()
                            .filesDir.absolutePath
                            .toPath()
                            .resolve("datastore")
                            .resolve("fr.outadoc.justchatting.preferences_pb")
                    },
                )
            }

            single<ConnectivityManager> { get<Context>().getSystemService()!! }
            single<BaseHttpClientProvider> { AndroidHttpClientProvider(get(), get()) }

            single<LogStrategy> {
                FileLogStrategy(
                    logFilePath = get(named("logFilePath")),
                    fileSystem = FileSystem.SYSTEM,
                )
            }
            single<LogRepository> {
                AndroidLogRepository(
                    applicationContext = get(),
                    logFilePath = get(named("logFilePath")),
                    dumpDirectory = get(named("logsDirectory")),
                    fileSystem = FileSystem.SYSTEM,
                    dispatchersProvider = get(),
                    clock = get(),
                )
            }
            single<AppVersionNameProvider> { AndroidAppVersionNameProvider(get()) }
            single<AppUpdateChecker> { NoopAppUpdateChecker() }
            single<LocalCallbackWebServer> { NoopLocalCallbackWebServer() }
            single<Connectivity> {
                Connectivity {
                    autoStart = true
                }
            }
        }
