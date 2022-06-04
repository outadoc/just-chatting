package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
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
                userImage.visible()
                userImage.loadImage(fragment, item.channelLogo, circle = true)
            } else {
                userImage.gone()
            }
            if (item.user_name != null) {
                username.visible()
                username.text = item.user_name
            } else {
                username.gone()
            }
            if (item.title != null && item.title != "") {
                title.visible()
                title.text = item.title.trim()
            } else {
                title.gone()
            }
            if (item.game_name != null) {
                gameName.visible()
                gameName.text = item.game_name
            } else {
                gameName.gone()
            }
        }
    }
}
