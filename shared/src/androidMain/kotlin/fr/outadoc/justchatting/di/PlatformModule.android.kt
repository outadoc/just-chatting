package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.mobile.AndroidChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.mobile.AndroidCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.preferences.data.DataStorePreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.presentation.AndroidLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.AndroidReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AndroidAppVersionNameProvider
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppVersionNameProvider
import fr.outadoc.justchatting.utils.core.AndroidNetworkStateObserver
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.http.AndroidHttpClientProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module
    get() = module {
        single<ChatNotifier> { AndroidChatNotifier(get(), get()) }
        single<CreateShortcutForChannelUseCase> { AndroidCreateShortcutForChannelUseCase(get()) }

        single<SqlDriver> {
            AndroidSqliteDriver(
                schema = AppDatabase.Schema,
                context = get(),
                name = "database",
            )
        }
        single<ConnectivityManager> { get<Context>().getSystemService()!! }
        single<NetworkStateObserver> { AndroidNetworkStateObserver(get()) }
        single<BaseHttpClientProvider> { AndroidHttpClientProvider(get(), get()) }

        single<LogRepository> { AndroidLogRepository(get()) }
        single<ReadExternalDependenciesList> { AndroidReadExternalDependenciesList(get()) }
        single<PreferenceRepository> { DataStorePreferenceRepository(get()) }
        single<AppVersionNameProvider> { AndroidAppVersionNameProvider(get()) }
    }
