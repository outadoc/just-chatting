package com.github.andreyasadchy.xtra.ui.downloads

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.common.BaseListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.*

class DownloadsAdapter(
    private val fragment: Fragment,
    private val clickListener: DownloadsFragment.OnVideoSelectedListener,
    private val channelClickListener: OnChannelSelectedListener,
    private val gameClickListener: GamesFragment.OnGameSelectedListener,
    private val deleteVideo: (OfflineVideo) -> Unit,
) : BaseListAdapter<OfflineVideo>(
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
                val text = TwitchApiHelper.formatTime(context, item.uploadDate)
                if (text != null) {
                    date.visible()
                    date.text = context.getString(R.string.uploaded_date, text)
                } else {
                    date.gone()
                }
            } else {
                date.gone()
            }
            downloadDate.text = context.getString(R.string.downloaded_date, TwitchApiHelper.formatTime(context, item.downloadDate))
            if (item.duration != null) {
                duration.visible()
                duration.text = DateUtils.formatElapsedTime(item.duration / 1000L)
            } else {
                duration.gone()
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
                userImage.loadImage(fragment, item.channelLogo, circle = true)
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
                progressBar.visible()
                progressBar.progress = (item.lastWatchPosition.toFloat() / item.duration * 100).toInt()
                item.sourceStartPosition?.let {
                    sourceStart.text = context.getString(R.string.source_vod_start, DateUtils.formatElapsedTime(it / 1000L))
                    sourceEnd.text = context.getString(R.string.source_vod_end, DateUtils.formatElapsedTime((it + item.duration) / 1000L))
                }
            } else {
                progressBar.gone()
            }
            options.setOnClickListener {
                PopupMenu(context, it).apply {
                    inflate(R.menu.offline_item)
                    setOnMenuItemClickListener { deleteVideo(item); true }
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