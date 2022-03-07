package com.github.andreyasadchy.xtra.repository

import android.content.Context
import com.github.andreyasadchy.xtra.db.LocalFollowsGameDao
import com.github.andreyasadchy.xtra.model.offline.LocalFollowGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFollowGameRepository @Inject constructor(
        private val localFollowsGameDao: LocalFollowsGameDao) {

    fun loadFollows() = localFollowsGameDao.getAll()

    suspend fun getFollowById(id: String) = withContext(Dispatchers.IO) {
        localFollowsGameDao.getById(id)
    }

    suspend fun saveFollow(item: LocalFollowGame) = withContext(Dispatchers.IO) {
        localFollowsGameDao.insert(item)
    }

    fun deleteFollow(context: Context, item: LocalFollowGame) {
        GlobalScope.launch {
            localFollowsGameDao.delete(item)
            File(context.filesDir.toString() + File.separator + "box_art" + File.separator + "${item.game_id}.png").delete()
        }
    }

    fun updateFollow(item: LocalFollowGame) {
        GlobalScope.launch { localFollowsGameDao.update(item) }
    }
}
