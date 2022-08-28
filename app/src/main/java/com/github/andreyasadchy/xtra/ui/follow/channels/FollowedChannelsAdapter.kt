package com.github.andreyasadchy.xtra.ui.follow.channels

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.NavigationHandler
import com.github.andreyasadchy.xtra.util.formatTime
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.userFollowed
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.userImage
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.userStream
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.username
import kotlinx.datetime.Instant

class FollowedChannelsAdapter(
    private val listener: NavigationHandler
) : BasePagedListAdapter<Follow>(

    object : DiffUtil.ItemCallback<Follow>() {
        override fun areItemsTheSame(oldItem: Follow, newItem: Follow): Boolean =
            oldItem.to_id == newItem.to_id

        override fun areContentsTheSame(oldItem: Follow, newItem: Follow): Boolean = true
    }) {

    override val layoutId: Int = R.layout.fragment_followed_channels_list_item

    override fun bind(item: Follow, view: View) {
        with(view) {
            setOnClickListener {
                listener.viewChannel(
                    item.to_id,
                    item.to_login,
                    item.to_name,
                    item.channelLogo
                )
            }

            if (item.channelLogo != null) {
                userImage.isVisible = true
                userImage.loadImage(
                    context,
                    item.channelLogo,
                    circle = true
                )
            } else {
                userImage.isVisible = false
            }

            if (item.to_name != null) {
                username.isVisible = true
                username.text = item.to_name
            } else {
                username.isVisible = false
            }

            val lastBroadcast = item.lastBroadcast
            if (lastBroadcast != null) {
                userStream.isVisible = true
                userStream.text = context.getString(
                    R.string.last_broadcast_date,
                    Instant.parse(lastBroadcast).formatTime(context)
                )
            } else {
                userStream.isVisible = false
            }

            val followedAt = item.followed_at
            if (followedAt != null) {
                userFollowed.isVisible = true
                userFollowed.text = context.getString(
                    R.string.followed_at,
                    Instant.parse(followedAt).formatTime(context)
                )
            } else {
                userFollowed.isVisible = false
            }
        }
    }
}
