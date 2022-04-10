package com.github.andreyasadchy.xtra.ui.saved.downloads

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import com.github.andreyasadchy.xtra.util.FetchProvider
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.PlaylistParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter
import javax.inject.Inject

class DownloadsViewModel @Inject internal constructor(
    application: Application,
    private val repository: OfflineRepository,
    private val fetchProvider: FetchProvider) : AndroidViewModel(application) {

    val list = repository.loadAllVideos()

    fun delete(context: Context, video: OfflineVideo) {
        repository.deleteVideo(context, video)
        GlobalScope.launch {
            if (video.status == OfflineVideo.STATUS_DOWNLOADED) {
                val playlistFile = File(video.url)
                if (!playlistFile.exists()) {
                    return@launch
                }
                if (video.vod) {
                    val directory = playlistFile.parentFile
                    val playlists = directory.listFiles(FileFilter { it.extension == "m3u8" && it != playlistFile })
                    if (playlists.isEmpty()) {
                        directory.deleteRecursively()
                    } else {
                        val playlist = PlaylistParser(playlistFile.inputStream(), Format.EXT_M3U, Encoding.UTF_8).parse()
                        val tracksToDelete = playlist.mediaPlaylist.tracks.toMutableSet()
                        playlists.forEach {
                            val p = PlaylistParser(it.inputStream(), Format.EXT_M3U, Encoding.UTF_8).parse()
                            tracksToDelete.removeAll(p.mediaPlaylist.tracks.toSet())
                        }
                        playlistFile.delete()
                        tracksToDelete.forEach { File(it.uri).delete() }
                    }
                } else {
                    playlistFile.delete()
                }
            } else {
                fetchProvider.get(video.id).deleteGroup(video.id)
            }
        }
    }
}