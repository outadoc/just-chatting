package com.github.andreyasadchy.xtra.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.andreyasadchy.xtra.model.VideoPosition
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.offline.*

@Database(entities = [OfflineVideo::class, Request::class, RecentEmote::class, VideoPosition::class, LocalFollowChannel::class, LocalFollowGame::class, Bookmark::class, VodBookmarkIgnoredUser::class, SortChannel::class, SortGame::class], version = 17)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
    abstract fun requests(): RequestsDao
    abstract fun recentEmotes(): RecentEmotesDao
    abstract fun videoPositions(): VideoPositionsDao
    abstract fun localFollowsChannel(): LocalFollowsChannelDao
    abstract fun localFollowsGame(): LocalFollowsGameDao
    abstract fun bookmarks(): BookmarksDao
    abstract fun vodBookmarkIgnoredUsers(): VodBookmarkIgnoredUsersDao
    abstract fun sortChannelDao(): SortChannelDao
    abstract fun sortGameDao(): SortGameDao
}