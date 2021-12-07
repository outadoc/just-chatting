package com.github.andreyasadchy.xtra.ui.clips

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_clips_list_item.view.*

class ClipsAdapter(
        private val fragment: Fragment,
        private val clickListener: BaseClipsFragment.OnClipSelectedListener,
        private val channelClickListener: OnChannelSelectedListener,
        private val showDownloadDialog: (Clip) -> Unit) : BasePagedListAdapter<Clip>(
        object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.view_count == newItem.view_count &&
                            oldItem.title == newItem.title

        }) {

    override val layoutId: Int = R.layout.fragment_clips_list_item

    override fun bind(item: Clip, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.broadcaster_id, item.broadcaster_login, item.broadcaster_name, item.channelLogo) }
        with(view) {
            setOnClickListener { clickListener.startClip(item) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(fragment, item.thumbnail, diskCacheStrategy = DiskCacheStrategy.NONE)
            date.text = TwitchApiHelper.formatTime(context, item.uploadDate)
            views.text = TwitchApiHelper.formatViewsCount(context, item.view_count, context.prefs().getBoolean(C.UI_VIEWCOUNT, false))
            duration.text = DateUtils.formatElapsedTime(item.duration.toLong())
            userImage.apply {
                loadImage(fragment, item.channelLogo, circle = true)
                setOnClickListener(channelListener)
            }
            title.text = item.title
            username.apply {
                setOnClickListener(channelListener)
                text = item.broadcaster_name
            }
            gameName.text = item.game
            options.setOnClickListener {
                PopupMenu(context, options).apply {
                    inflate(R.menu.media_item)
                    setOnMenuItemClickListener {
                        showDownloadDialog(item)
                        return@setOnMenuItemClickListener true
                    }
                    show()
                }
            }
        }
    }
}