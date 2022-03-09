package com.github.andreyasadchy.xtra.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.andreyasadchy.xtra.db.*
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.repository.LocalFollowGameRepository
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providesRepository(videosDao: VideosDao, requestsDao: RequestsDao, localFollowsChannelDao: LocalFollowsChannelDao): OfflineRepository = OfflineRepository(videosDao, requestsDao, localFollowsChannelDao)

    @Singleton
    @Provides
    fun providesLocalFollowsChannelRepository(localFollowsChannelDao: LocalFollowsChannelDao, videosDao: VideosDao): LocalFollowChannelRepository = LocalFollowChannelRepository(localFollowsChannelDao, videosDao)

    @Singleton
    @Provides
    fun providesLocalFollowsGameRepository(localFollowsGameDao: LocalFollowsGameDao): LocalFollowGameRepository = LocalFollowGameRepository(localFollowsGameDao)

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
    fun providesLocalFollowsChannelDao(database: AppDatabase): LocalFollowsChannelDao = database.localFollowsChannel()

    @Singleton
    @Provides
    fun providesLocalFollowsGameDao(database: AppDatabase): LocalFollowsGameDao = database.localFollowsGame()

    @Singleton
    @Provides
    fun providesAppDatabase(application: Application): AppDatabase =
            Room.databaseBuilder(application, AppDatabase::class.java, "database")
                    .addMigrations(
                        object : Migration(9, 10) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL("DELETE FROM emotes")
                            }
                        },
                        object : Migration(10, 11) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL("ALTER TABLE videos ADD COLUMN videoId TEXT DEFAULT null")
                            }
                        },
                        object : Migration(11, 12) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL("CREATE TABLE IF NOT EXISTS local_follows_games (game_id TEXT NOT NULL, game_name TEXT, boxArt TEXT, PRIMARY KEY (game_id))")
                            }
                        }
                    )
                    .build()
}
