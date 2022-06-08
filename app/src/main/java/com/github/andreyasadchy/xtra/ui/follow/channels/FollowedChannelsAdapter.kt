package com.github.andreyasadchy.xtra.ui.follow.channels

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.localText
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.twitchText
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.userFollowed
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.userImage
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.userStream
import kotlinx.android.synthetic.main.fragment_followed_channels_list_item.view.username

class FollowedChannelsAdapter(
    private val fragment: Fragment,
    private val listener: OnChannelSelectedListener
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
                    item.channelLogo,
                    item.followLocal
                )
            }
            if (item.channelLogo != null) {
                userImage.isVisible = true
                userImage.loadImage(
                    context,
                    item.channelLogo,
                    circle = true,
                    diskCacheStrategy = DiskCacheStrategy.NONE
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
            if (item.lastBroadcast != null) {
                val text = item.lastBroadcast?.let { TwitchApiHelper.formatTimeString(context, it) }
                if (text != null) {
                    userStream.isVisible = true
                    userStream.text = context.getString(R.string.last_broadcast_date, text)
                } else {
                    userStream.isVisible = false
                }
            } else {
                userStream.isVisible = false
            }
            if (item.followed_at != null) {
                val text = TwitchApiHelper.formatTimeString(context, item.followed_at!!)
                if (text != null) {
                    userFollowed.isVisible = true
                    userFollowed.text = context.getString(R.string.followed_at, text)
                } else {
                    userFollowed.isVisible = false
                }
            } else {
                userFollowed.isVisible = false
            }
            if (item.followTwitch) {
                twitchText.isVisible = true
            } else {
                twitchText.isVisible = false
            }
            if (item.followLocal) {
                localText.isVisible = true
            } else {
                localText.isVisible = false
            }
        }
    }
}
