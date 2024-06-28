package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.component.chatapi.db.AppDatabase
import fr.outadoc.justchatting.component.chatapi.db.DbRecentChannelsDao
import fr.outadoc.justchatting.component.chatapi.db.DbRecentEmotesDao
import fr.outadoc.justchatting.component.chatapi.db.RecentChannelQueries
import fr.outadoc.justchatting.component.chatapi.db.RecentChannelsDao
import fr.outadoc.justchatting.component.chatapi.db.RecentEmoteQueries
import fr.outadoc.justchatting.component.chatapi.db.RecentEmotesDao
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
    single<RecentChannelsDao> { DbRecentChannelsDao(get()) }
}
