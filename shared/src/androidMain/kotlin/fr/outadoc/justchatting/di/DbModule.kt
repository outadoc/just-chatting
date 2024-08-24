package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.data.db.PronounQueries
import fr.outadoc.justchatting.data.db.RecentEmoteQueries
import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.data.db.UserQueries
import fr.outadoc.justchatting.feature.emotes.data.db.RecentEmotesDb
import fr.outadoc.justchatting.feature.emotes.domain.RecentEmotesApi
import fr.outadoc.justchatting.feature.pronouns.data.LocalPronounsDb
import fr.outadoc.justchatting.feature.pronouns.domain.LocalPronounsApi
import fr.outadoc.justchatting.feature.shared.data.LocalStreamsDb
import fr.outadoc.justchatting.feature.shared.data.LocalUsersDb
import fr.outadoc.justchatting.feature.shared.domain.LocalStreamsApi
import fr.outadoc.justchatting.feature.shared.domain.LocalUsersApi
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
    single<LocalPronounsApi> { LocalPronounsDb(get(), get()) }
}
