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
import kotlinx.datetime.toInstant

class FollowedChannelsAdapter(
    private val listener: NavigationHandler
) : BasePagedListAdapter<Follow>(

    object : DiffUtil.ItemCallback<Follow>() {
        override fun areItemsTheSame(oldItem: Follow, newItem: Follow): Boolean =
            oldItem.toId == newItem.toId

        override fun areContentsTheSame(oldItem: Follow, newItem: Follow): Boolean = true
    }) {

    override fun getItemViewType(position: Int): Int = R.layout.fragment_followed_channels_list_item

    override fun bind(item: Follow, view: View) {
        val binding = FragmentFollowedChannelsListItemBinding.bind(view)
        with(binding) {
            view.setOnClickListener {
                item.toLogin?.let { login ->
                    listener.viewChannel(login)
                }
            }

            if (item.profileImageURL != null) {
                userImage.isVisible = true
                userImage.loadImage(
                    item.profileImageURL,
                    circle = true
                )
            } else {
                userImage.isVisible = false
            }

            if (item.toName != null) {
                username.isVisible = true
                username.text = item.toName
            } else {
                username.isVisible = false
            }

            val followedAt = item.followedAt
            if (followedAt != null) {
                userFollowed.isVisible = true
                userFollowed.text = view.context.getString(
                    R.string.followed_at,
                    followedAt.toInstant().formatTime(view.context)
                )
            } else {
                userFollowed.isVisible = false
            }
        }
    }
}
