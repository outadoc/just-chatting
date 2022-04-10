package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.db.BookmarksDao
import com.github.andreyasadchy.xtra.model.offline.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarksRepository @Inject constructor(
    private val bookmarksDao: BookmarksDao) {

    fun loadBookmarks() = bookmarksDao.getAll()

    suspend fun getBookmarkById(id: String) = withContext(Dispatchers.IO) {
        bookmarksDao.getById(id)
    }

    suspend fun saveBookmark(item: Bookmark) = withContext(Dispatchers.IO) {
        bookmarksDao.insert(item)
    }

    fun deleteBookmark(item: Bookmark) {
        GlobalScope.launch { bookmarksDao.delete(item) }
    }
}
