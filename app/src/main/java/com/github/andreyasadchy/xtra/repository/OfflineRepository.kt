package com.github.andreyasadchy.xtra.repository

import android.content.Context
import com.github.andreyasadchy.xtra.XtraApp
import com.github.andreyasadchy.xtra.db.BookmarksDao
import com.github.andreyasadchy.xtra.db.LocalFollowsChannelDao
import com.github.andreyasadchy.xtra.db.RequestsDao
import com.github.andreyasadchy.xtra.db.VideosDao
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.model.offline.Request
import com.github.andreyasadchy.xtra.ui.download.DownloadService
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.DownloadUtils
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao,
        private val requestsDao: RequestsDao,
        private val localFollowsChannelDao: LocalFollowsChannelDao,
        private val bookmarksDao: BookmarksDao) {

    fun loadAllVideos() = videosDao.getAll()

    suspend fun getVideoById(id: Int) = withContext(Dispatchers.IO) {
        videosDao.getById(id)
    }

    suspend fun getVideosByUserId(id: Int) = withContext(Dispatchers.IO) {
        videosDao.getByUserId(id)
    }

    suspend fun saveVideo(video: OfflineVideo) = withContext(Dispatchers.IO) {
        videosDao.insert(video)
    }

    fun deleteVideo(context: Context, video: OfflineVideo) {
        GlobalScope.launch {
            if (!video.videoId.isNullOrBlank() && bookmarksDao.getById(video.videoId) == null) {
                File(context.filesDir.toString() + File.separator + "thumbnails" + File.separator + "${video.videoId}.png").delete()
            }
            if (!video.channelId.isNullOrBlank() && localFollowsChannelDao.getById(video.channelId) == null && bookmarksDao.getByUserId(video.channelId).isNullOrEmpty()) {
                File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${video.channelId}.png").delete()
            }
            videosDao.delete(video)
        }
    }

    fun updateVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.update(video) }
    }

    fun updateVideoPosition(id: Int, position: Long) {
        val appContext = XtraApp.INSTANCE.applicationContext
        if (appContext.prefs().getBoolean(C.PLAYER_USE_VIDEOPOSITIONS, true)) {
            GlobalScope.launch { videosDao.updatePosition(id, position) }
        }
    }

    fun resumeDownloads(context: Context, wifiOnly: Boolean) {
        GlobalScope.launch {
            requestsDao.getAll().forEach {
                if (DownloadService.activeRequests.add(it.offlineVideoId)) {
                    DownloadUtils.download(context, it, wifiOnly)
                }
            }
        }
    }

    fun saveRequest(request: Request) {
        GlobalScope.launch { requestsDao.insert(request) }
    }

    fun deleteRequest(request: Request) {
        GlobalScope.launch { requestsDao.delete(request) }
    }
}
