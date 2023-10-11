package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import fr.outadoc.justchatting.component.chatapi.db.AppDatabase
import fr.outadoc.justchatting.component.chatapi.db.DbRecentEmotesRepository
import fr.outadoc.justchatting.component.chatapi.db.RecentEmoteQueries
import fr.outadoc.justchatting.component.chatapi.db.RecentEmotesRepository
import org.koin.dsl.module

val dbModule = module {
    single { AppDatabase(get<SqlDriver>()) }
    single<RecentEmoteQueries> { get<AppDatabase>().recentEmoteQueries }
    single<RecentEmotesRepository> { DbRecentEmotesRepository(get()) }
}
