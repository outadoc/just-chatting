package fr.outadoc.justchatting.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.AppVersionNameProvider
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import net.harawata.appdirs.AppDirsFactory
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

        single<ChatNotifier> { TODO() }
        single<CreateShortcutForChannelUseCase> { TODO() }

        single<SqlDriver> { TODO() }

        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    AppDirsFactory.getInstance()
                        .getUserConfigDir(
                            "JustChatting",
                            "1.0.0",
                            "outadoc",
                            true,
                        )
                        .toPath()
                        .resolve("datastore")
                        .resolve("fr.outadoc.justchatting.preferences_pb")
                },
            )
        }

        single<BaseHttpClientProvider> { TODO() }

        single<LogRepository> { TODO() }
        single<AppVersionNameProvider> { TODO() }
    }
