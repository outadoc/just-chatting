package com.github.andreyasadchy.xtra.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.andreyasadchy.xtra.model.VideoPosition
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.offline.LocalFollow
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.model.offline.Request

@Database(entities = [OfflineVideo::class, Request::class, RecentEmote::class, VideoPosition::class, LocalFollow::class], version = 11)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
    abstract fun requests(): RequestsDao
    abstract fun recentEmotes(): RecentEmotesDao
    abstract fun videoPositions(): VideoPositionsDao
    abstract fun localFollows(): LocalFollowsDao
}