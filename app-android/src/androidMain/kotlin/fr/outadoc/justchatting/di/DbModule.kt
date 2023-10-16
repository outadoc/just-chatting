package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.component.chatapi.db.AppDatabase
import fr.outadoc.justchatting.component.chatapi.db.DbRecentEmotesRepository
import fr.outadoc.justchatting.component.chatapi.db.RecentEmoteQueries
import fr.outadoc.justchatting.component.chatapi.db.RecentEmotesRepository
import org.koin.dsl.module

val dbModule = module {

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = get(),
            name = "database",
        )
    }

    single { AppDatabase(get<SqlDriver>()) }
    single<RecentEmoteQueries> { get<AppDatabase>().recentEmoteQueries }
    single<RecentEmotesRepository> { DbRecentEmotesRepository(get()) }
}
