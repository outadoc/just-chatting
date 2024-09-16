package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import fr.outadoc.justchatting.component.preferences.domain.UserDefaultsPreferenceRepository
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.NoopChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.NoopCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.presentation.AppleReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.NoopLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.NoopReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppleAppVersionNameProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.core.NoopNetworkStateObserver
import fr.outadoc.justchatting.utils.http.AppleHttpClientProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import org.koin.core.module.Module
import org.koin.dsl.module

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

        single<NetworkStateObserver> { NoopNetworkStateObserver() }
        single<BaseHttpClientProvider> { AppleHttpClientProvider(get()) }

        single<LogRepository> { NoopLogRepository() }
        single<ReadExternalDependenciesList> { NoopReadExternalDependenciesList() }
        single<PreferenceRepository> { UserDefaultsPreferenceRepository() }
        single<AppVersionNameProvider> { AppleAppVersionNameProvider() }
        single<ReadExternalDependenciesList> { AppleReadExternalDependenciesList() }
    }
