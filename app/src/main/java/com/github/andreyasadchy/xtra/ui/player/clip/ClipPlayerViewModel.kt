package com.github.andreyasadchy.xtra.ui.player.clip

import android.app.Application
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.GraphQLRepositoy
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.ui.player.PlayerHelper
import com.github.andreyasadchy.xtra.ui.player.PlayerViewModel
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.shortToast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ClipPlayerViewModel"

class ClipPlayerViewModel @Inject constructor(
        context: Application,
        private val graphQLRepositoy: GraphQLRepositoy,
        private val repository: TwitchService) : PlayerViewModel(context), FollowViewModel {

    private lateinit var clip: Clip
    private val factory: ProgressiveMediaSource.Factory = ProgressiveMediaSource.Factory(dataSourceFactory)
    private val prefs = context.prefs()
    private val helper = PlayerHelper()
    val qualities: Map<String, String>
        get() = helper.urls
    val loaded: LiveData<Boolean>
        get() = helper.loaded
    override val channelId: String
        get() = clip.broadcaster_id
    private val _video = MutableLiveData<Video>()
    val video: LiveData<Video>
        get() = _video
    private var loadingVideo = false

    override lateinit var follow: FollowLiveData

    override fun changeQuality(index: Int) {
        playbackPosition = player.currentPosition
        val quality = helper.urls.values.elementAt(index)
        play(quality)
        prefs.edit { putString(C.PLAYER_QUALITY, helper.urls.keys.elementAt(index)) }
        qualityIndex = index
    }

    override fun onResume() {
        super.onResume()
        player.seekTo(playbackPosition)
    }

    override fun onPause() {
        playbackPosition = player.currentPosition
        super.onPause()
    }

    fun setClip(clip: Clip) {
        if (!this::clip.isInitialized) {
            this.clip = clip
            viewModelScope.launch {
                try {
                    val urls = graphQLRepositoy.loadClipUrls(prefs.getString(C.GQL_CLIENT_ID, "") ?: "", clip.id)
                    val savedquality = prefs.getString(C.PLAYER_QUALITY, "720p60")
                    if (savedquality != null) {
                        var url: String? = null
                        for (entry in urls.entries.withIndex()) {
                            if (entry.value.key == savedquality) {
                                url = entry.value.value
                                qualityIndex = entry.index
                                break
                            }
                        }
                        url.let {
                            if (it != null) {
                                play(it)
                            } else {
                                play(urls.values.first())
                            }
                        }
                    } else {
                        play(urls.values.first())
                    }
                    helper.urls = urls
                    helper.loaded.value = true
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelId, viewModelScope)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        if (error.type == ExoPlaybackException.TYPE_UNEXPECTED && error.unexpectedException is IllegalStateException) {
            val context = getApplication<Application>()
            context.shortToast(R.string.player_error)
            if (qualityIndex < helper.urls.size - 1) {
                changeQuality(++qualityIndex)
            }
        }
    }

    fun loadVideo(clientId: String?, token: String?) {
        if (!loadingVideo) {
            loadingVideo = true
            viewModelScope.launch {
                try {
                    val video = repository.loadVideo(clientId, token, clip.video_id).data.first()
                    _video.postValue(video)
                } catch (e: Exception) {

                } finally {
                    loadingVideo = false
                }
            }
        }
    }

    private fun play(url: String) {
        mediaSource = factory.createMediaSource(url.toUri())
        play()
        player.seekTo(playbackPosition)
    }
}
