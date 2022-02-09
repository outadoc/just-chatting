package com.github.andreyasadchy.xtra.ui.follow.channels

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.*

class FollowedChannelsAdapter(
        private val fragment: Fragment,
        private val listener: OnChannelSelectedListener) : BasePagedListAdapter<Follow>(
        object : DiffUtil.ItemCallback<Follow>() {
            override fun areItemsTheSame(oldItem: Follow, newItem: Follow): Boolean =
                    oldItem.to_id == newItem.to_id

            override fun areContentsTheSame(oldItem: Follow, newItem: Follow): Boolean = true
        }) {

    override val layoutId: Int = R.layout.fragment_followed_channels_list_item

    override fun bind(item: Follow, view: View) {
        with(view) {
            setOnClickListener { listener.viewChannel(item.to_id, item.to_login, item.to_name, item.channelLogo, item.followLocal) }
            if (item.channelLogo != null)  {
                userImage.visible()
                userImage.loadImage(fragment, item.channelLogo, circle = true)
            }
            if (item.to_name != null)  {
                username.visible()
                username.text = item.to_name
            }
            if (item.followTwitch) {
                twitchText.visible()
            }
            if (item.followLocal) {
                localText.visible()
            }
        }
    }
}