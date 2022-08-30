package fr.outadoc.justchatting.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fr.outadoc.justchatting.db.AppDatabase
import fr.outadoc.justchatting.db.BookmarksDao
import fr.outadoc.justchatting.db.LocalFollowsChannelDao
import fr.outadoc.justchatting.db.LocalFollowsGameDao
import fr.outadoc.justchatting.db.RecentEmotesDao
import fr.outadoc.justchatting.db.RequestsDao
import fr.outadoc.justchatting.db.SortChannelDao
import fr.outadoc.justchatting.db.SortGameDao
import fr.outadoc.justchatting.db.VideoPositionsDao
import fr.outadoc.justchatting.db.VideosDao
import fr.outadoc.justchatting.repository.LocalFollowChannelRepository
import fr.outadoc.justchatting.repository.SortChannelRepository
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providesLocalFollowsChannelRepository(
        localFollowsChannelDao: LocalFollowsChannelDao,
        videosDao: VideosDao,
        bookmarksDao: BookmarksDao
    ): LocalFollowChannelRepository =
        LocalFollowChannelRepository(localFollowsChannelDao, videosDao, bookmarksDao)

    @Singleton
    @Provides
    fun providesSortChannelRepository(sortChannelDao: SortChannelDao): SortChannelRepository =
        SortChannelRepository(sortChannelDao)

    @Singleton
    @Provides
    fun providesVideosDao(database: AppDatabase): VideosDao = database.videos()

    @Singleton
    @Provides
    fun providesRequestsDao(database: AppDatabase): RequestsDao = database.requests()

    @Singleton
    @Provides
    fun providesRecentEmotesDao(database: AppDatabase): RecentEmotesDao = database.recentEmotes()

    @Singleton
    @Provides
    fun providesVideoPositions(database: AppDatabase): VideoPositionsDao = database.videoPositions()

    @Singleton
    @Provides
    fun providesLocalFollowsChannelDao(database: AppDatabase): LocalFollowsChannelDao =
        database.localFollowsChannel()

    @Singleton
    @Provides
    fun providesLocalFollowsGameDao(database: AppDatabase): LocalFollowsGameDao =
        database.localFollowsGame()

    @Singleton
    @Provides
    fun providesBookmarksDao(database: AppDatabase): BookmarksDao = database.bookmarks()

    @Singleton
    @Provides
    fun providesSortChannelDao(database: AppDatabase): SortChannelDao = database.sortChannelDao()

    @Singleton
    @Provides
    fun providesSortGameDao(database: AppDatabase): SortGameDao = database.sortGameDao()

    @Singleton
    @Provides
    fun providesAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "database").build()
}
