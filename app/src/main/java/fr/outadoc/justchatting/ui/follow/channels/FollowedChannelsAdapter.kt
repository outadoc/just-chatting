package fr.outadoc.justchatting.ui.follow.channels

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentFollowedChannelsListItemBinding
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.util.formatTime
import fr.outadoc.justchatting.util.loadImage
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
        val binding = FragmentFollowedChannelsListItemBinding.bind(view)
        with(binding) {
            view.setOnClickListener {
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
                userStream.text = view.context.getString(
                    R.string.last_broadcast_date,
                    Instant.parse(lastBroadcast).formatTime(view.context)
                )
            } else {
                userStream.isVisible = false
            }

            val followedAt = item.followed_at
            if (followedAt != null) {
                userFollowed.isVisible = true
                userFollowed.text = view.context.getString(
                    R.string.followed_at,
                    Instant.parse(followedAt).formatTime(view.context)
                )
            } else {
                userFollowed.isVisible = false
            }
        }
    }
}
