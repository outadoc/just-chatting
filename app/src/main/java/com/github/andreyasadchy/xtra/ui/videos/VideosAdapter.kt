package com.github.andreyasadchy.xtra.ui.videos

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.common.OnGameSelectedListener
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.fragment_videos_list_item.view.*

class VideosAdapter(
        private val fragment: Fragment,
        private val clickListener: BaseVideosFragment.OnVideoSelectedListener,
        private val gameClickListener: OnGameSelectedListener,
        private val channelClickListener: OnChannelSelectedListener,
        private val showDownloadDialog: (Video) -> Unit) : BaseVideosAdapter(
        object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.view_count == newItem.view_count &&
                            oldItem.thumbnail_url == newItem.thumbnail_url &&
                            oldItem.title == newItem.title &&
                            oldItem.duration == newItem.duration
        }) {

    override val layoutId: Int = R.layout.fragment_videos_list_item

    override fun bind(item: Video, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.user_id, item.user_login, item.user_name, item.channelLogo) }
        val gameListener: (View) -> Unit = { gameClickListener.openGame(item.gameId, item.gameName) }
        with(view) {
            val getDuration = item.duration?.let { TwitchApiHelper.getDuration(it) }
            val position = positions?.get(item.id.toLong())
            setOnClickListener { clickListener.startVideo(item, position?.toDouble()) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(fragment, item.thumbnail, diskCacheStrategy = DiskCacheStrategy.NONE)
            date.text = item.createdAt?.let { TwitchApiHelper.formatTimeString(context, it) }
            views.text = item.view_count?.let { TwitchApiHelper.formatViewsCount(context, it, context.prefs().getBoolean(C.UI_VIEWCOUNT, false)) }
            duration.text = getDuration?.let { DateUtils.formatElapsedTime(it) }
            type.text = TwitchApiHelper.getType(context, item.videoType)
            position.let {
                if (it != null && getDuration != null && getDuration > 0L) {
                    progressBar.progress = (it / (getDuration * 10L)).toInt()
                    progressBar.visible()
                } else {
                    progressBar.gone()
                }
            }
            if (item.channelLogo != null)  {
                userImage.visible()
                userImage.loadImage(fragment, item.channelLogo, circle = true)
                userImage.setOnClickListener(channelListener)
            }
            if (item.user_name != null)  {
                username.visible()
                username.text = item.user_name
                username.setOnClickListener(channelListener)
            }
            if (item.title != null)  {
                title.visible()
                title.text = item.title.trim()
            }
            if (item.gameName != null)  {
                gameName.visible()
                gameName.text = item.gameName
                gameName.setOnClickListener(gameListener)
            }
            options.setOnClickListener {
                PopupMenu(context, it).apply {
                    inflate(R.menu.media_item)
                    setOnMenuItemClickListener { showDownloadDialog(item); true }
                    show()
                }
            }
        }
    }
}