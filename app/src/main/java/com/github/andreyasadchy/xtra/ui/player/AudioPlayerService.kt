package com.github.andreyasadchy.xtra.ui.player

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.GlideApp
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.VideoPosition
import com.github.andreyasadchy.xtra.player.lowlatency.DefaultHlsPlaylistParserFactory
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistTracker
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.android.exoplayer2.util.Util
import dagger.android.AndroidInjection
import javax.inject.Inject

class AudioPlayerService : Service() {

    @Inject
    lateinit var playerRepository: PlayerRepository

    private lateinit var playlistUrl: Uri

    private lateinit var player: ExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private var restorePosition = false
    private var type = -1
    private var videoId: Number? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        player = ExoPlayer.Builder(this).setTrackSelector(DefaultTrackSelector(this).apply {
            parameters = buildUponParameters().setRendererDisabled(0, true).build()
        }).build()
    }

    override fun onDestroy() {
        when (type) {
            TYPE_VIDEO -> {
                position = player.currentPosition
                playerRepository.saveVideoPosition(VideoPosition(videoId as Long, position))
            }
        }
        player.release()
        connection = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        val channelId = getString(R.string.notification_playback_channel_id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, getString(R.string.notification_playback_channel_title), NotificationManager.IMPORTANCE_LOW)
                channel.setSound(null, null)
                manager.createNotificationChannel(channel)
            }
        }
        playlistUrl = intent.getStringExtra(KEY_PLAYLIST_URL)!!.toUri()
        createMediaSource()
        var currentPlaybackPosition = intent.getLongExtra(KEY_CURRENT_POSITION, 0L)
        val usePlayPause = intent.getBooleanExtra(KEY_USE_PLAY_PAUSE, false)
        type = intent.getIntExtra(KEY_TYPE, -1)
        when (type) {
            TYPE_VIDEO -> videoId = intent.getLongExtra(KEY_VIDEO_ID, -1L)
            TYPE_OFFLINE -> videoId = intent.getIntExtra(KEY_VIDEO_ID, -1)
        }
        player.apply {
            addListener(object : Player.Listener  {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (restorePosition && playbackState == Player.STATE_READY) {
                        restorePosition = false
                        player.seekTo(currentPlaybackPosition)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    if (usePlayPause && !restorePosition) { //if it's a vod and didn't already save position
                        currentPlaybackPosition = player.currentPosition
                        restorePosition = true
                    }
                    setMediaSource(mediaSource)
                    prepare()
                }
            })
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            if (currentPlaybackPosition > 0) {
                player.seekTo(currentPlaybackPosition)
            }
        }
        playerNotificationManager = CustomPlayerNotificationManager(
                this,
                channelId,
                System.currentTimeMillis().toInt(),
                DescriptionAdapter(intent.getStringExtra(KEY_TITLE), intent.getStringExtra(KEY_CHANNEL_NAME) ?: "", intent.getStringExtra(KEY_IMAGE_URL) ?: ""),
                object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                        startForeground(notificationId, notification)
                    }

                    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        } else {
                            stopForeground(true)
                        }
                    }
                },
            !usePlayPause,
            null,
            smallIconResourceId = R.drawable.baseline_audiotrack_black_24,
            playActionIconResourceId = R.drawable.exo_notification_play,
            pauseActionIconResourceId = R.drawable.exo_notification_pause,
            stopActionIconResourceId = R.drawable.exo_notification_stop,
            rewindActionIconResourceId = R.drawable.exo_notification_rewind,
            fastForwardActionIconResourceId = R.drawable.exo_notification_fastforward,
            previousActionIconResourceId = R.drawable.exo_notification_previous,
            nextActionIconResourceId = R.drawable.exo_notification_next,
            null
        ).apply {
            setUseNextAction(false)
            setUsePreviousAction(false)
            setUsePlayPauseActions(usePlayPause)
            setUseStopAction(true)
            setUseFastForwardAction(false)
            setUseRewindAction(false)
        }
        return AudioBinder()
    }

    private fun createMediaSource() {
        mediaSource = HlsMediaSource.Factory(DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name))))
                .setAllowChunklessPreparation(true)
                .setPlaylistParserFactory(DefaultHlsPlaylistParserFactory())
                .setPlaylistTrackerFactory(DefaultHlsPlaylistTracker.FACTORY)
                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))
                .createMediaSource(MediaItem.fromUri(playlistUrl))
    }

    inner class AudioBinder : Binder() {

        val player: ExoPlayer
            get() = this@AudioPlayerService.player

        fun showNotification() {
            playerNotificationManager.setPlayer(player)
        }

        fun hideNotification() {
            playerNotificationManager.setPlayer(null)
        }

        fun restartPlayer() {
            player.stop()
            createMediaSource()
            player.setMediaSource(mediaSource)
            player.prepare()
        }
    }

    private class CustomPlayerNotificationManager(context: Context, channelId: String, notificationId: Int, mediaDescriptionAdapter: MediaDescriptionAdapter, notificationListener: NotificationListener, private val isLive: Boolean, customActionReceiver: CustomActionReceiver?, smallIconResourceId: Int, playActionIconResourceId: Int, pauseActionIconResourceId: Int, stopActionIconResourceId: Int, rewindActionIconResourceId: Int, fastForwardActionIconResourceId: Int, previousActionIconResourceId: Int, nextActionIconResourceId: Int, groupKey: String?) : PlayerNotificationManager(context, channelId, notificationId, mediaDescriptionAdapter, notificationListener, customActionReceiver, smallIconResourceId, playActionIconResourceId, pauseActionIconResourceId, stopActionIconResourceId, rewindActionIconResourceId, fastForwardActionIconResourceId, previousActionIconResourceId, nextActionIconResourceId, groupKey) {
        override fun createNotification(player: Player, builder: NotificationCompat.Builder?, ongoing: Boolean, largeIcon: Bitmap?): NotificationCompat.Builder? {
            return super.createNotification(player, builder, ongoing, largeIcon)?.apply { mActions[if (isLive) 0 else 1].icon = R.drawable.baseline_close_black_36 }
        }

        override fun getActionIndicesForCompactView(actionNames: List<String>, player: Player): IntArray {
            return if (isLive) intArrayOf(0) else intArrayOf(0, 1)
        }
    }

    private inner class DescriptionAdapter(
            private val text: String?,
            private val title: String,
            private val imageUrl: String) : PlayerNotificationManager.MediaDescriptionAdapter {

        private var largeIcon: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val clickIntent = Intent(this@AudioPlayerService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.KEY_CODE, MainActivity.INTENT_OPEN_PLAYER)
            }
            return PendingIntent.getActivity(this@AudioPlayerService, REQUEST_CODE_RESUME, clickIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
        }

        override fun getCurrentContentText(player: Player): String? = text

        override fun getCurrentContentTitle(player: Player): String = title

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            if (largeIcon == null) {
                try {
                    GlideApp.with(this@AudioPlayerService)
                            .asBitmap()
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    callback.onBitmap(resource)
                                    largeIcon = resource
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                } catch (e: Exception) {

                }
            }
            return largeIcon
        }
    }

    companion object {
        const val KEY_PLAYLIST_URL = "playlistUrl"
        const val KEY_CHANNEL_NAME = "channelName"
        const val KEY_TITLE = "title"
        const val KEY_IMAGE_URL = "imageUrl"
        const val KEY_USE_PLAY_PAUSE = "playPause"
        const val KEY_CURRENT_POSITION = "currentPosition"
        const val KEY_TYPE = "type"
        const val KEY_VIDEO_ID = "videoId"

        const val REQUEST_CODE_RESUME = 2

        const val TYPE_STREAM = 0
        const val TYPE_VIDEO = 1
        const val TYPE_OFFLINE = 2

        var connection: ServiceConnection? = null
        var position = 0L
    }
}