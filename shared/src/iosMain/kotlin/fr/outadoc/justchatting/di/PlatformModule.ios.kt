package fr.outadoc.justchatting.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.NoopChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.NoopCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.preferences.presentation.AppleReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.NoopLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppleAppVersionNameProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.core.NoopNetworkStateObserver
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

internal actual val platformModule: Module
    get() = module {
        single<ChatNotifier> { NoopChatNotifier() }
        single<CreateShortcutForChannelUseCase> { NoopCreateShortcutForChannelUseCase() }

        single<SqlDriver> {
            NativeSqliteDriver(
                schema = AppDatabase.Schema,
                name = "database",
            )
        }

        @OptIn(ExperimentalForeignApi::class)
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    val documentDirectory: Path =
                        NSFileManager.defaultManager
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

                    documentDirectory
                        .resolve("fr.outadoc.justchatting.preferences_pb")
                },
            )
        }

        single<NetworkStateObserver> { NoopNetworkStateObserver() }
        single<BaseHttpClientProvider> { AppleHttpClientProvider(get(), get()) }

        single<LogRepository> { NoopLogRepository() }
        single<AppVersionNameProvider> { AppleAppVersionNameProvider() }
        single<ReadExternalDependenciesList> { AppleReadExternalDependenciesList() }
    }
