package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.gameName
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.title
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.userImage
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.username

abstract class BaseStreamsAdapter(
    protected val fragment: Fragment,
    private val clickListener: BaseStreamsFragment.OnStreamSelectedListener
) : BasePagedListAdapter<Stream>(
    object : DiffUtil.ItemCallback<Stream>() {
        override fun areItemsTheSame(oldItem: Stream, newItem: Stream): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Stream, newItem: Stream): Boolean =
            oldItem.viewer_count == newItem.viewer_count &&
                oldItem.game_name == newItem.game_name &&
                oldItem.title == newItem.title
    }) {

    override fun bind(item: Stream, view: View) {
        with(view) {
            setOnClickListener { clickListener.startStream(item) }
            if (item.channelLogo != null) {
                userImage.isVisible = true
                userImage.loadImage(context, item.channelLogo, circle = true)
            } else {
                userImage.isVisible = false
            }
            if (item.user_name != null) {
                username.isVisible = true
                username.text = item.user_name
            } else {
                username.isVisible = false
            }
            if (item.title != null && item.title != "") {
                title.isVisible = true
                title.text = item.title.trim()
            } else {
                title.isVisible = false
            }
            if (item.game_name != null) {
                gameName.isVisible = true
                gameName.text = item.game_name
            } else {
                gameName.isVisible = false
            }
        }
    }
}
