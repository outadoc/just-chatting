package com.github.andreyasadchy.xtra.repository

import android.content.Context
import com.github.andreyasadchy.xtra.db.BookmarksDao
import com.github.andreyasadchy.xtra.db.LocalFollowsChannelDao
import com.github.andreyasadchy.xtra.db.VideosDao
import com.github.andreyasadchy.xtra.model.offline.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarksRepository @Inject constructor(
    private val bookmarksDao: BookmarksDao,
    private val localFollowsChannelDao: LocalFollowsChannelDao,
    private val videosDao: VideosDao) {

    fun loadBookmarksLiveData() = bookmarksDao.getAllLiveData()

    suspend fun loadBookmarks() = withContext(Dispatchers.IO) {
        bookmarksDao.getAll()
    }

    suspend fun getBookmarkById(id: String) = withContext(Dispatchers.IO) {
        bookmarksDao.getById(id)
    }

    suspend fun getBookmarksByUserId(id: String) = withContext(Dispatchers.IO) {
        bookmarksDao.getByUserId(id)
    }

    suspend fun saveBookmark(item: Bookmark) = withContext(Dispatchers.IO) {
        bookmarksDao.insert(item)
    }

    fun deleteBookmark(context: Context, item: Bookmark) {
        GlobalScope.launch {
            if (item.id.isNotBlank() && videosDao.getById(item.id.toInt()) == null) {
                File(context.filesDir.toString() + File.separator + "thumbnails" + File.separator + "${item.id}.png").delete()
            }
            if (!item.userId.isNullOrBlank() && localFollowsChannelDao.getById(item.userId) == null && videosDao.getByUserId(item.userId.toInt()).isNullOrEmpty()) {
                File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${item.userId}.png").delete()
            }
            bookmarksDao.delete(item)
        }
    }

    fun updateBookmark(item: Bookmark) {
        GlobalScope.launch { bookmarksDao.update(item) }
    }
}
