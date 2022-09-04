package fr.outadoc.justchatting.db

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.outadoc.justchatting.model.VideoPosition
import fr.outadoc.justchatting.model.chat.RecentEmote
import fr.outadoc.justchatting.model.offline.Bookmark
import fr.outadoc.justchatting.model.offline.OfflineVideo
import fr.outadoc.justchatting.model.offline.Request
import fr.outadoc.justchatting.model.offline.SortChannel
import fr.outadoc.justchatting.model.offline.SortGame
import fr.outadoc.justchatting.model.offline.VodBookmarkIgnoredUser

@Database(
    entities = [
        OfflineVideo::class,
        Request::class,
        RecentEmote::class,
        VideoPosition::class,
        Bookmark::class,
        VodBookmarkIgnoredUser::class,
        SortChannel::class,
        SortGame::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
    abstract fun requests(): RequestsDao
    abstract fun recentEmotes(): RecentEmotesDao
    abstract fun videoPositions(): VideoPositionsDao
    abstract fun bookmarks(): BookmarksDao
    abstract fun sortChannelDao(): SortChannelDao
    abstract fun sortGameDao(): SortGameDao
}
