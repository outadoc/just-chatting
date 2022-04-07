package com.github.andreyasadchy.xtra.ui.downloads

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.model.offline.VodBookmarkIgnoredUser
import com.github.andreyasadchy.xtra.repository.*
import com.github.andreyasadchy.xtra.ui.videos.offline.BaseOfflineViewModel
import com.github.andreyasadchy.xtra.util.FetchProvider
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.PlaylistParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter
import javax.inject.Inject

class DownloadsViewModel @Inject constructor(
    playerRepository: PlayerRepository,
    private val repository: TwitchService,
    private val offlineRepository: OfflineRepository,
    private val fetchProvider: FetchProvider,
    private val vodBookmarkIgnoredUsersRepository: VodBookmarkIgnoredUsersRepository) : BaseOfflineViewModel(playerRepository) {

    val offlineVideos = offlineRepository.loadAllVideosLiveData()

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<OfflineVideo>> = Transformations.map(filter) {
        repository.loadOfflineVideos(it.helixClientId, it.helixToken, it.gqlClientId, it.vodTimeLeft, it.currentList, viewModelScope)
    }

    fun loadVideos(helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, vodTimeLeft: Boolean? = null, currentList: List<OfflineVideo>?) {
        Filter(helixClientId, helixToken, gqlClientId, vodTimeLeft, currentList).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
        val helixClientId: String?,
        val helixToken: String?,
        val gqlClientId: String?,
        val vodTimeLeft: Boolean?,
        val currentList: List<OfflineVideo>?)

    fun deleteDownload(context: Context, video: OfflineVideo) {
        offlineRepository.deleteVideo(context, video)
        if (video.bookmark != true) {
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

    fun vodIgnoreUser(userId: String) {
        GlobalScope.launch {
            if (vodBookmarkIgnoredUsersRepository.getUserById(userId) != null) {
                vodBookmarkIgnoredUsersRepository.deleteUser(VodBookmarkIgnoredUser(userId))
            } else {
                vodBookmarkIgnoredUsersRepository.saveUser(VodBookmarkIgnoredUser(userId))
            }
        }
    }
}