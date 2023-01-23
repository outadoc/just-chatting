package fr.outadoc.justchatting.component.chatapi.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecentEmote::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentEmotes(): RecentEmotesDao
}
