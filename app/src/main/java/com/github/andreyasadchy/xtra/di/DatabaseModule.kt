package com.github.andreyasadchy.xtra.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.andreyasadchy.xtra.db.*
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providesRepository(videosDao: VideosDao, requestsDao: RequestsDao, localFollowsDao: LocalFollowsDao): OfflineRepository = OfflineRepository(videosDao, requestsDao, localFollowsDao)

    @Singleton
    @Provides
    fun providesLocalFollowsRepository(localFollowsDao: LocalFollowsDao, videosDao: VideosDao): LocalFollowRepository = LocalFollowRepository(localFollowsDao, videosDao)

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
    fun providesLocalFollowsDao(database: AppDatabase): LocalFollowsDao = database.localFollows()

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
                        }
                    )
                    .build()
}
