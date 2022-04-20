package com.github.andreyasadchy.xtra.ui.saved.downloads

import android.text.format.DateUtils
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.common.BaseListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.*

class DownloadsAdapter(
    private val fragment: Fragment,
    private val clickListener: DownloadsFragment.OnVideoSelectedListener,
    private val channelClickListener: OnChannelSelectedListener,
    private val gameClickListener: GamesFragment.OnGameSelectedListener,
    private val deleteVideo: (OfflineVideo) -> Unit) : BaseListAdapter<OfflineVideo>(
    object : DiffUtil.ItemCallback<OfflineVideo>() {
        override fun areItemsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
            return false //bug, oldItem and newItem are sometimes the same
        }
    }) {

    override val layoutId: Int = R.layout.fragment_downloads_list_item

    override fun bind(item: OfflineVideo, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.channelId, item.channelLogin, item.channelName, item.channelLogo, updateLocal = true) }
        val gameListener: (View) -> Unit = { gameClickListener.openGame(item.gameId, item.gameName) }
        with(view) {
            setOnClickListener { clickListener.startOfflineVideo(item) }
            setOnLongClickListener { deleteVideo(item); true }
            thumbnail.loadImage(fragment, item.thumbnail, diskCacheStrategy = DiskCacheStrategy.AUTOMATIC)
            if (item.uploadDate != null) {
                date.visible()
                date.text = context.getString(R.string.uploaded_date, TwitchApiHelper.formatTime(context, item.uploadDate))
            } else {
                date.gone()
            }
            if (item.downloadDate != null) {
                downloadDate.visible()
                downloadDate.text = context.getString(R.string.downloaded_date, TwitchApiHelper.formatTime(context, item.downloadDate))
            } else {
                downloadDate.gone()
            }
            if (item.type != null) {
                val text = TwitchApiHelper.getType(context, item.type)
                if (text != null) {
                    type.visible()
                    type.text = text
                } else {
                    type.gone()
                }
            } else {
                type.gone()
            }
            if (item.channelLogo != null)  {
                userImage.visible()
                userImage.loadImage(fragment, item.channelLogo, circle = true, diskCacheStrategy = DiskCacheStrategy.NONE)
                userImage.setOnClickListener(channelListener)
            } else {
                userImage.gone()
            }
            if (item.channelName != null)  {
                username.visible()
                username.text = item.channelName
                username.setOnClickListener(channelListener)
            } else {
                username.gone()
            }
            if (item.name != null)  {
                title.visible()
                title.text = item.name.trim()
            } else {
                title.gone()
            }
            if (item.gameName != null)  {
                gameName.visible()
                gameName.text = item.gameName
                gameName.setOnClickListener(gameListener)
            } else {
                gameName.gone()
            }
            if (item.duration != null) {
                duration.visible()
                duration.text = DateUtils.formatElapsedTime(item.duration / 1000L)
                if (item.sourceStartPosition != null)  {
                    sourceStart.visible()
                    sourceStart.text = context.getString(R.string.source_vod_start, DateUtils.formatElapsedTime(item.sourceStartPosition / 1000L))
                    sourceEnd.visible()
                    sourceEnd.text = context.getString(R.string.source_vod_end, DateUtils.formatElapsedTime((item.sourceStartPosition + item.duration) / 1000L))
                } else {
                    sourceStart.gone()
                    sourceEnd.gone()
                }
                if (context.prefs().getBoolean(C.PLAYER_USE_VIDEOPOSITIONS, true) && item.lastWatchPosition != null && item.duration > 0L) {
                    progressBar.progress = (item.lastWatchPosition!!.toFloat() / item.duration * 100).toInt()
                    progressBar.visible()
                } else {
                    progressBar.gone()
                }
            } else {
                duration.gone()
                sourceStart.gone()
                sourceEnd.gone()
                progressBar.gone()
            }
            if (sourceEnd.isVisible && sourceStart.isVisible) {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, context.convertDpToPixels(5F), 0, 0)
                sourceEnd.layoutParams = params
            }
            if (type.isVisible && (sourceStart.isVisible || sourceEnd.isVisible)) {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, context.convertDpToPixels(5F), 0, 0)
                type.layoutParams = params
            }
            options.setOnClickListener { it ->
                PopupMenu(context, it).apply {
                    inflate(R.menu.offline_item)
                    setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.delete -> deleteVideo(item)
                            else -> menu.close()
                        }
                        true
                    }
                    show()
                }
            }
            status.apply {
                if (item.status == OfflineVideo.STATUS_DOWNLOADED) {
                    gone()
                } else {
                    text = if (item.status == OfflineVideo.STATUS_DOWNLOADING) {
                        context.getString(R.string.downloading_progress, ((item.progress.toFloat() / item.maxProgress) * 100f).toInt())
                    } else {
                        context.getString(R.string.download_pending)
                    }
                    visible()
                    setOnClickListener { deleteVideo(item) }
                    setOnLongClickListener { deleteVideo(item); true }
                }
            }
        }
    }
}