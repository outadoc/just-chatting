package fr.outadoc.justchatting.repository

import android.content.Context
import fr.outadoc.justchatting.db.BookmarksDao
import fr.outadoc.justchatting.db.LocalFollowsChannelDao
import fr.outadoc.justchatting.db.VideosDao
import fr.outadoc.justchatting.model.offline.LocalFollowChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LocalFollowChannelRepository(
    private val localFollowsChannelDao: LocalFollowsChannelDao,
    private val videosDao: VideosDao,
    private val bookmarksDao: BookmarksDao
) {

    fun loadFollows() = localFollowsChannelDao.getAll()

    suspend fun getFollowById(id: String) = withContext(Dispatchers.IO) {
        localFollowsChannelDao.getById(id)
    }

    suspend fun saveFollow(item: LocalFollowChannel) = withContext(Dispatchers.IO) {
        localFollowsChannelDao.insert(item)
    }

    fun deleteFollow(context: Context, item: LocalFollowChannel) {
        GlobalScope.launch {
            if (item.user_id.isNotBlank() && bookmarksDao.getByUserId(item.user_id)
                    .isNullOrEmpty() && videosDao.getByUserId(item.user_id.toInt()).isNullOrEmpty()
            ) {
                File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${item.user_id}.png").delete()
            }
            localFollowsChannelDao.delete(item)
        }
    }

    fun updateFollow(item: LocalFollowChannel) {
        GlobalScope.launch { localFollowsChannelDao.update(item) }
    }
}
