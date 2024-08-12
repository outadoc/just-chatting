package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.feature.recent.data.RecentChannelsDb
import fr.outadoc.justchatting.feature.recent.data.RecentEmotesDb
import fr.outadoc.justchatting.feature.recent.domain.RecentChannelsApi
import fr.outadoc.justchatting.feature.recent.domain.RecentEmotesApi
import org.koin.core.module.Module
import org.koin.dsl.module

public val dbModule: Module = module {

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = get(),
            name = "database",
        )
    }

    single { AppDatabase(get<SqlDriver>()) }

    single<RecentEmoteQueries> { get<AppDatabase>().recentEmoteQueries }
    single<RecentEmotesApi> { RecentEmotesDb(get()) }

    single<RecentChannelQueries> { get<AppDatabase>().recentChannelQueries }
    single<RecentChannelsApi> { RecentChannelsDb(get()) }
}
