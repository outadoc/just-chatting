package fr.outadoc.justchatting.component.twitch.domain.inject

import androidx.room.Room
import fr.outadoc.justchatting.component.chatapi.db.AppDatabase
import org.koin.dsl.module

val dbModule = module {
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "database").build()
    }
}
