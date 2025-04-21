package fr.outadoc.justchatting.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import fr.outadoc.justchatting.AppInfo
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.NoopChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.NoopCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.NoopLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.DesktopAppVersionNameProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import fr.outadoc.justchatting.utils.http.DesktopHttpClientProvider
import net.harawata.appdirs.AppDirsFactory
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module
    get() = module {
        val appDir = AppDirsFactory.getInstance()
            .getUserConfigDir(
                AppInfo.APP_ID,
                AppInfo.APP_VERSION,
                AppInfo.APP_AUTHOR,
                true,
            )
            .toPath()

        single {
            OAuthAppCredentials(
                clientId = "l9klwmh97qgn0s0me276ezsft5szp2",
                redirectUri = "https://just-chatting.app/auth/callback.html",
            )
        }

        single<ChatNotifier> { NoopChatNotifier() }
        single<CreateShortcutForChannelUseCase> { NoopCreateShortcutForChannelUseCase() }

        single<SqlDriver> {
            val dbPath = appDir.resolve("database.db").toString()
            JdbcSqliteDriver("jdbc:sqlite:${dbPath}")
                .also { driver ->
                    AppDatabase.Schema.create(driver)
                }
        }

        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    appDir
                        .resolve("datastore")
                        .resolve("fr.outadoc.justchatting.preferences_pb")
                },
            )
        }

        single<BaseHttpClientProvider> { DesktopHttpClientProvider(get(), get()) }

        single<LogRepository> { NoopLogRepository() }
        single<AppVersionNameProvider> { DesktopAppVersionNameProvider() }
    }
