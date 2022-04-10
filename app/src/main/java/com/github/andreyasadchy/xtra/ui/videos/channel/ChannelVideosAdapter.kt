package com.github.andreyasadchy.xtra.ui.videos.channel

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosAdapter
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_videos_list_item.view.*

class ChannelVideosAdapter(
    private val fragment: Fragment,
    private val clickListener: BaseVideosFragment.OnVideoSelectedListener,
    private val gameClickListener: GamesFragment.OnGameSelectedListener,
    private val showDownloadDialog: (Video) -> Unit,
    private val saveBookmark: (Video) -> Unit) : BaseVideosAdapter(
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
        val gameListener: (View) -> Unit = { gameClickListener.openGame(item.gameId, item.gameName) }
        with(view) {
            val getDuration = item.duration?.let { TwitchApiHelper.getDuration(it) }
            val position = positions?.get(item.id.toLong())
            setOnClickListener { clickListener.startVideo(item, position?.toDouble()) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(fragment, item.thumbnail, diskCacheStrategy = DiskCacheStrategy.NONE)
            if (item.createdAt != null) {
                val text = TwitchApiHelper.formatTimeString(context, item.createdAt)
                if (text != null) {
                    date.visible()
                    date.text = text
                } else {
                    date.gone()
                }
            } else {
                date.gone()
            }
            if (item.view_count != null) {
                views.visible()
                views.text = TwitchApiHelper.formatViewsCount(context, item.view_count)
            } else {
                views.gone()
            }
            if (getDuration != null) {
                duration.visible()
                duration.text = DateUtils.formatElapsedTime(getDuration)
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
            if (position != null && getDuration != null && getDuration > 0L) {
                progressBar.progress = (position / (getDuration * 10)).toInt()
                progressBar.visible()
            } else {
                progressBar.gone()
            }
            if (item.title != null && item.title != "")  {
                title.visible()
                title.text = item.title.trim()
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
            options.setOnClickListener { it ->
                PopupMenu(context, it).apply {
                    inflate(R.menu.media_item)
                    if (item.id.isNotBlank()) {
                        menu.findItem(R.id.bookmark).isVisible = true
                        if (bookmarks?.find { it.id == item.id } != null) {
                            menu.findItem(R.id.bookmark).title = context.getString(R.string.remove_bookmark)
                        } else {
                            menu.findItem(R.id.bookmark).title = context.getString(R.string.add_bookmark)
                        }
                    }
                    setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.download -> showDownloadDialog(item)
                            R.id.bookmark -> saveBookmark(item)
                            else -> menu.close()
                        }
                        true
                    }
                    show()
                }
            }
        }
    }
}