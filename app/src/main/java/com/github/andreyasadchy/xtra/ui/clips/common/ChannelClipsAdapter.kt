package com.github.andreyasadchy.xtra.ui.clips.common

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_videos_list_item.view.*

class ChannelClipsAdapter(
    private val fragment: Fragment,
    private val clickListener: BaseClipsFragment.OnClipSelectedListener,
    private val gameClickListener: GamesFragment.OnGameSelectedListener,
    private val showDownloadDialog: (Clip) -> Unit) : BasePagedListAdapter<Clip>(
        object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.view_count == newItem.view_count &&
                            oldItem.title == newItem.title

        }) {

    override val layoutId: Int = R.layout.fragment_videos_list_item

    override fun bind(item: Clip, view: View) {
        val gameListener: (View) -> Unit = { gameClickListener.openGame(item.gameId, item.gameName) }
        with(view) {
            setOnClickListener { clickListener.startClip(item) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(fragment, item.thumbnail, diskCacheStrategy = DiskCacheStrategy.NONE)
            if (item.uploadDate != null) {
                val text = item.uploadDate?.let { TwitchApiHelper.formatTimeString(context, it) }
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
            if (item.duration != null) {
                duration.visible()
                duration.text = DateUtils.formatElapsedTime(item.duration.toLong())
            } else {
                duration.gone()
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
                    setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.download -> showDownloadDialog(item)
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
