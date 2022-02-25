package com.github.andreyasadchy.xtra.repository

import android.content.Context
import com.github.andreyasadchy.xtra.db.LocalFollowsDao
import com.github.andreyasadchy.xtra.db.VideosDao
import com.github.andreyasadchy.xtra.model.offline.LocalFollow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFollowRepository @Inject constructor(
        private val localFollowsDao: LocalFollowsDao,
        private val videosDao: VideosDao) {

    fun loadFollows() = localFollowsDao.getAll()

    suspend fun getFollowById(id: String) = withContext(Dispatchers.IO) {
        localFollowsDao.getById(id)
    }

    suspend fun saveFollow(item: LocalFollow) = withContext(Dispatchers.IO) {
        localFollowsDao.insert(item)
    }

    fun deleteFollow(context: Context, item: LocalFollow) {
        GlobalScope.launch {
            localFollowsDao.delete(item)
            if (videosDao.getByUserId(item.user_id.toInt()).isNullOrEmpty()) {
                File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${item.user_id}.png").delete()
            }
        }
    }

    fun updateFollow(item: LocalFollow) {
        GlobalScope.launch { localFollowsDao.update(item) }
    }
}
