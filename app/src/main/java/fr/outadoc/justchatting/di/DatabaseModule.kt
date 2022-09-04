package fr.outadoc.justchatting.di

import androidx.room.Room
import fr.outadoc.justchatting.db.AppDatabase
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.PlayerRepository
import fr.outadoc.justchatting.repository.SortChannelRepository
import org.koin.dsl.module

val databaseModule = module {

    single { SortChannelRepository(get()) }
    single { AuthRepository(get(), get(), get()) }
    single { PlayerRepository(get(), get()) }

    single { get<AppDatabase>().bookmarks() }
    single { get<AppDatabase>().recentEmotes() }
    single { get<AppDatabase>().requests() }
    single { get<AppDatabase>().sortChannelDao() }
    single { get<AppDatabase>().sortGameDao() }
    single { get<AppDatabase>().videoPositions() }
    single { get<AppDatabase>().videos() }

    single { Room.databaseBuilder(get(), AppDatabase::class.java, "database").build() }
}
