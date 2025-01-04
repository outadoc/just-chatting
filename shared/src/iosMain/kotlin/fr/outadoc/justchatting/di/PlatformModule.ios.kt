package fr.outadoc.justchatting.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.NoopChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.NoopCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.NoopLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppleAppVersionNameProvider
import fr.outadoc.justchatting.utils.http.AppleHttpClientProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual val platformModule: Module
    get() = module {
        single {
            OAuthAppCredentials(
                clientId = "rzpd86ie5dz4hghlvcgtfgwmvyzfz2",
                redirectUri = "https://just-chatting.app/auth/callback.html",
            )
        }

        single<ChatNotifier> { NoopChatNotifier() }
        single<CreateShortcutForChannelUseCase> { NoopCreateShortcutForChannelUseCase() }

        single<SqlDriver> {
            NativeSqliteDriver(
                schema = AppDatabase.Schema,
                name = "database",
            )
        }

        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    getDocumentsDirectory().resolve("fr.outadoc.justchatting.preferences_pb")
                },
            )
        }

        single<BaseHttpClientProvider> { AppleHttpClientProvider(get(), get()) }

        single<LogRepository> { NoopLogRepository() }
        single<AppVersionNameProvider> { AppleAppVersionNameProvider() }
    }

@OptIn(ExperimentalForeignApi::class)
private fun getDocumentsDirectory(): Path {
    return NSFileManager.defaultManager
        .URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        ?.path
        ?.toPath()
        ?: error("Could not get document directory")
}
