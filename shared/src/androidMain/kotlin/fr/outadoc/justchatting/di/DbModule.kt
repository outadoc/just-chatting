package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.data.db.RecentChannelQueries
import fr.outadoc.justchatting.data.db.RecentEmoteQueries
import fr.outadoc.justchatting.feature.emotes.data.recent.DbRecentEmotesDao
import fr.outadoc.justchatting.feature.emotes.domain.recent.RecentEmotesDao
import fr.outadoc.justchatting.feature.recent.data.RecentChannelsDb
import fr.outadoc.justchatting.feature.recent.domain.RecentChannelsApi
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
    single<RecentEmotesDao> { DbRecentEmotesDao(get()) }

    single<RecentChannelQueries> { get<AppDatabase>().recentChannelQueries }
    single<RecentChannelsApi> { RecentChannelsDb(get()) }
}
