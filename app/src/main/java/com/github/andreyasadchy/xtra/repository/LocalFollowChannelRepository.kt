package com.github.andreyasadchy.xtra.repository

import android.content.Context
import com.github.andreyasadchy.xtra.db.BookmarksDao
import com.github.andreyasadchy.xtra.db.LocalFollowsChannelDao
import com.github.andreyasadchy.xtra.db.VideosDao
import com.github.andreyasadchy.xtra.model.offline.LocalFollowChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFollowChannelRepository @Inject constructor(
    private val localFollowsChannelDao: LocalFollowsChannelDao,
    private val videosDao: VideosDao,
    private val bookmarksDao: BookmarksDao) {

    fun loadFollows() = localFollowsChannelDao.getAll()

    suspend fun getFollowById(id: String) = withContext(Dispatchers.IO) {
        localFollowsChannelDao.getById(id)
    }

    suspend fun saveFollow(item: LocalFollowChannel) = withContext(Dispatchers.IO) {
        localFollowsChannelDao.insert(item)
    }

    fun deleteFollow(context: Context, item: LocalFollowChannel) {
        GlobalScope.launch {
            if (item.user_id.isNotBlank() && bookmarksDao.getByUserId(item.user_id).isNullOrEmpty() && videosDao.getByUserId(item.user_id.toInt()).isNullOrEmpty()) {
                File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${item.user_id}.png").delete()
            }
            localFollowsChannelDao.delete(item)
        }
    }

    fun updateFollow(item: LocalFollowChannel) {
        GlobalScope.launch { localFollowsChannelDao.update(item) }
    }
}
