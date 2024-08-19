package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.data.db.PronounQueries
import fr.outadoc.justchatting.data.db.RecentEmoteQueries
import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.data.db.UserQueries
import fr.outadoc.justchatting.feature.home.data.db.LocalStreamsDb
import fr.outadoc.justchatting.feature.home.data.db.LocalUsersDb
import fr.outadoc.justchatting.feature.pronouns.data.db.LocalPronounsApi
import fr.outadoc.justchatting.feature.pronouns.data.db.LocalPronounsDb
import fr.outadoc.justchatting.feature.recent.data.RecentEmotesDb
import fr.outadoc.justchatting.feature.recent.domain.LocalStreamsApi
import fr.outadoc.justchatting.feature.recent.domain.LocalUsersApi
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

    single<UserQueries> { get<AppDatabase>().userQueries }
    single<LocalUsersApi> { LocalUsersDb(get(), get()) }

    single<StreamQueries> { get<AppDatabase>().streamQueries }
    single<LocalStreamsApi> { LocalStreamsDb(get(), get()) }

    single<PronounQueries> { get<AppDatabase>().pronounQueries }
    single<LocalPronounsApi> { LocalPronounsDb(get()) }
}
