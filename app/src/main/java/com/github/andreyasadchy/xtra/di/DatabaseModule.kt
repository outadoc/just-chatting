package com.github.andreyasadchy.xtra.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.andreyasadchy.xtra.db.*
import com.github.andreyasadchy.xtra.repository.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providesRepository(videosDao: VideosDao, requestsDao: RequestsDao, localFollowsChannelDao: LocalFollowsChannelDao, bookmarksDao: BookmarksDao): OfflineRepository = OfflineRepository(videosDao, requestsDao, localFollowsChannelDao, bookmarksDao)

    @Singleton
    @Provides
    fun providesLocalFollowsChannelRepository(localFollowsChannelDao: LocalFollowsChannelDao, videosDao: VideosDao, bookmarksDao: BookmarksDao): LocalFollowChannelRepository = LocalFollowChannelRepository(localFollowsChannelDao, videosDao, bookmarksDao)

    @Singleton
    @Provides
    fun providesLocalFollowsGameRepository(localFollowsGameDao: LocalFollowsGameDao): LocalFollowGameRepository = LocalFollowGameRepository(localFollowsGameDao)

    @Singleton
    @Provides
    fun providesBookmarksRepository(bookmarksDao: BookmarksDao, localFollowsChannelDao: LocalFollowsChannelDao, videosDao: VideosDao): BookmarksRepository = BookmarksRepository(bookmarksDao, localFollowsChannelDao, videosDao)

    @Singleton
    @Provides
    fun providesVodBookmarkIgnoredUsersRepository(vodBookmarkIgnoredUsersDao: VodBookmarkIgnoredUsersDao): VodBookmarkIgnoredUsersRepository = VodBookmarkIgnoredUsersRepository(vodBookmarkIgnoredUsersDao)

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
    fun providesBookmarksDao(database: AppDatabase): BookmarksDao = database.bookmarks()

    @Singleton
    @Provides
    fun providesVodBookmarkIgnoredUsersDao(database: AppDatabase): VodBookmarkIgnoredUsersDao = database.vodBookmarkIgnoredUsers()

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
                        },
                        object : Migration(12, 13) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL("CREATE TABLE IF NOT EXISTS videos1 (url TEXT NOT NULL, source_url TEXT NOT NULL, source_start_position INTEGER, name TEXT, channel_id TEXT, channel_login TEXT, channel_name TEXT, channel_logo TEXT, thumbnail TEXT, gameId TEXT, gameName TEXT, duration INTEGER, upload_date INTEGER, download_date INTEGER NOT NULL, last_watch_position INTEGER, progress INTEGER NOT NULL, max_progress INTEGER NOT NULL, status INTEGER NOT NULL, type TEXT, videoId TEXT, id INTEGER NOT NULL, is_vod INTEGER NOT NULL, PRIMARY KEY (id))")
                                database.execSQL("INSERT INTO videos1 (url, source_url, source_start_position, name, channel_id, channel_login, channel_name, channel_logo, thumbnail, gameId, gameName, duration, upload_date, download_date, last_watch_position, progress, max_progress, status, type, videoId, id, is_vod) SELECT url, source_url, source_start_position, name, channel_id, channel_login, channel_name, channel_logo, thumbnail, gameId, gameName, duration, upload_date, download_date, last_watch_position, progress, max_progress, status, type, videoId, id, is_vod FROM videos")
                                database.execSQL("DROP TABLE videos")
                                database.execSQL("ALTER TABLE videos1 RENAME TO videos")
                            }
                        },
                        object : Migration(13, 14) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL("CREATE TABLE IF NOT EXISTS videos1 (url TEXT NOT NULL, source_url TEXT, source_start_position INTEGER, name TEXT, channel_id TEXT, channel_login TEXT, channel_name TEXT, channel_logo TEXT, thumbnail TEXT, gameId TEXT, gameName TEXT, duration INTEGER, upload_date INTEGER, download_date INTEGER, last_watch_position INTEGER, progress INTEGER NOT NULL, max_progress INTEGER NOT NULL, status INTEGER, type TEXT, videoId TEXT, is_bookmark INTEGER, userType TEXT, id INTEGER NOT NULL, is_vod INTEGER NOT NULL, PRIMARY KEY (id))")
                                database.execSQL("INSERT INTO videos1 (url, source_url, source_start_position, name, channel_id, channel_login, channel_name, channel_logo, thumbnail, gameId, gameName, duration, upload_date, download_date, last_watch_position, progress, max_progress, status, type, videoId, id, is_vod) SELECT url, source_url, source_start_position, name, channel_id, channel_login, channel_name, channel_logo, thumbnail, gameId, gameName, duration, upload_date, download_date, last_watch_position, progress, max_progress, status, type, videoId, id = id, is_vod = is_vod FROM videos")
                                database.execSQL("DROP TABLE videos")
                                database.execSQL("ALTER TABLE videos1 RENAME TO videos")
                                database.execSQL("CREATE TABLE IF NOT EXISTS vod_bookmark_ignored_users (user_id TEXT NOT NULL, PRIMARY KEY (user_id))")
                            }
                        },
                        object : Migration(14, 15) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL("CREATE TABLE IF NOT EXISTS videos1 (url TEXT NOT NULL, source_url TEXT, source_start_position INTEGER, name TEXT, channel_id TEXT, channel_login TEXT, channel_name TEXT, channel_logo TEXT, thumbnail TEXT, gameId TEXT, gameName TEXT, duration INTEGER, upload_date INTEGER, download_date INTEGER, last_watch_position INTEGER, progress INTEGER NOT NULL, max_progress INTEGER NOT NULL, status INTEGER NOT NULL, type TEXT, videoId TEXT, id INTEGER NOT NULL, is_vod INTEGER NOT NULL, PRIMARY KEY (id))")
                                database.execSQL("INSERT OR IGNORE INTO videos1 (url, source_url, source_start_position, name, channel_id, channel_login, channel_name, channel_logo, thumbnail, gameId, gameName, duration, upload_date, download_date, last_watch_position, progress, max_progress, status, type, videoId, id, is_vod) SELECT url, source_url, source_start_position, name, channel_id, channel_login, channel_name, channel_logo, thumbnail, gameId, gameName, duration, upload_date, download_date, last_watch_position, progress, max_progress, status, type, videoId, id = id, is_vod = is_vod FROM videos")
                                database.execSQL("DROP TABLE videos")
                                database.execSQL("ALTER TABLE videos1 RENAME TO videos")
                                database.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (id TEXT NOT NULL, userId TEXT, userLogin TEXT, userName TEXT, userLogo TEXT, gameId TEXT, gameName TEXT, title TEXT, createdAt TEXT, thumbnail TEXT, type TEXT, duration TEXT, PRIMARY KEY (id))")
                            }
                        }
                    )
                    .build()
}
