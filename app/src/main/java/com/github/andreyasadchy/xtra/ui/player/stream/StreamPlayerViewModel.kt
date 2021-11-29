package com.github.andreyasadchy.xtra.ui.player.stream

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.player.AudioPlayerService
import com.github.andreyasadchy.xtra.ui.player.HlsPlayerViewModel
import com.github.andreyasadchy.xtra.ui.player.PlayerMode.*
import com.github.andreyasadchy.xtra.util.toast
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistTracker
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


class StreamPlayerViewModel @Inject constructor(
    context: Application,
    private val playerRepository: PlayerRepository,
    repository: TwitchService
) : HlsPlayerViewModel(context, repository) {

    private val _stream = MutableLiveData<Stream>()
    val stream: LiveData<Stream>
        get() = _stream
    override val channelId: String
        get() {
            return _stream.value!!.user_id
        }

    private var useAdBlock = false
    private var randomDeviceId = true
    private var xdeviceid = ""
    private var deviceid = ""
    private var playerType = ""
    private var gqlclientId = ""

    private val hlsMediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
        .setAllowChunklessPreparation(true)
        .setPlaylistParserFactory(DefaultHlsPlaylistParserFactory())
        .setPlaylistTrackerFactory(DefaultHlsPlaylistTracker.FACTORY)
        .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))

    fun startStream(clientId: String?, token: String, stream: Stream, useAdBlock: Boolean, randomDeviceId: Boolean, xdeviceid: String, deviceid: String, playerType: String, gqlclientId: String) {
        this.useAdBlock = useAdBlock
        this.randomDeviceId = randomDeviceId
        this.xdeviceid = xdeviceid
        this.deviceid = deviceid
        this.playerType = playerType
        this.gqlclientId = gqlclientId
        if (_stream.value == null) {
            _stream.value = stream
            loadStream(stream)
            viewModelScope.launch {
                while (isActive) {
                    try {
                        val s = repository.loadStream(clientId, token, stream.user_id).data.first()
                        _stream.postValue(s)
                        delay(300000L)
                    } catch (e: Exception) {
                        delay(60000L)
                    }
                }
            }
        }
    }

    override fun changeQuality(index: Int) {
        previousQuality = qualityIndex
        super.changeQuality(index)
        when {
            index < qualities.size - 2 -> setVideoQuality(index)
            index < qualities.size - 1 -> {
                (player.currentManifest as? HlsManifest)?.let {
                    val s = _stream.value!!
                    startBackgroundAudio(helper.urls.values.last(), s.user_name, s.title, "", false, AudioPlayerService.TYPE_STREAM, null)
                    _playerMode.value = AUDIO_ONLY
                }
            }
            else -> {
                if (playerMode.value == NORMAL) {
                    player.stop()
                } else {
                    stopBackgroundAudio()
                }
                _playerMode.value = DISABLED
            }
        }
    }

    override fun onResume() {
        isResumed = true
        if (playerMode.value == NORMAL) {
            loadStream(stream.value ?: return)
        } else if (playerMode.value == AUDIO_ONLY) {
            hideBackgroundAudio()
        }
    }

    override fun restartPlayer() {
        if (playerMode.value == NORMAL) {
            loadStream(stream.value ?: return)
        } else if (playerMode.value == AUDIO_ONLY) {
            binder?.restartPlayer()
        }
    }

    private fun loadStream(stream: Stream) {
        viewModelScope.launch {
            try {
                val result = playerRepository.loadStreamPlaylistUrl(gqlclientId, stream.user_login, playerType, useAdBlock, randomDeviceId, xdeviceid, deviceid)
                if (useAdBlock) {
                    if (result.second) {
                        httpDataSourceFactory.setDefaultRequestProperties(hashMapOf("X-Donate-To" to "https://ttv.lol/donate"))
                    } else {
                        val context = getApplication<Application>()
                        context.toast(R.string.adblock_not_working)
                    }
                }
                mediaSource = hlsMediaSourceFactory.createMediaSource(MediaItem.fromUri(result.first))
                play()
            } catch (e: Exception) {
                val context = getApplication<Application>()
                context.toast(R.string.error_stream)
            }
        }
    }

    override fun setSpeed(speed: Float, save: Boolean) {}
}
