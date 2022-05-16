package com.github.andreyasadchy.xtra.ui.download

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.offline.Request
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class ClipDownloadViewModel @Inject constructor(
    application: Application,
    private val graphQLRepository: GraphQLRepository,
    private val offlineRepository: OfflineRepository
) : AndroidViewModel(application) {

    private val _qualities = MutableLiveData<Map<String, String>>()
    val qualities: LiveData<Map<String, String>>
        get() = _qualities

    private lateinit var clip: Clip

    fun init(clientId: String?, clip: Clip, qualities: Map<String, String>?) {
        if (!this::clip.isInitialized) {
            this.clip = clip
            if (qualities == null) {
                viewModelScope.launch {
                    try {
                        val urls = graphQLRepository.loadClipUrls(clientId, clip.id)
                        _qualities.postValue(urls)
                    } catch (e: Exception) {

                    }
                }
            } else {
                _qualities.value = qualities!!
            }
        }
    }

    fun download(url: String, path: String, quality: String) {
        GlobalScope.launch {
            val context = getApplication<Application>()

            val filePath = "$path${File.separator}${clip.id}$quality"
            val startPosition = clip.duration.let { (it?.times(1000.0))?.toLong() }

            val offlineVideo = DownloadUtils.prepareDownload(context, clip, url, filePath, clip.duration?.toLong()?.times(1000L), startPosition)
            val videoId = offlineRepository.saveVideo(offlineVideo).toInt()
            val request = Request(videoId, url, offlineVideo.url)
            offlineRepository.saveRequest(request)

            DownloadUtils.download(context, request)
        }
    }
}