package com.github.andreyasadchy.xtra.ui.search.channels

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_search_channels_list_item.view.*

class ChannelSearchAdapter(
        private val fragment: Fragment,
        private val listener: OnChannelSelectedListener) : BasePagedListAdapter<ChannelSearch>(
        object : DiffUtil.ItemCallback<ChannelSearch>() {
            override fun areItemsTheSame(oldItem: ChannelSearch, newItem: ChannelSearch): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ChannelSearch, newItem: ChannelSearch): Boolean = true
        }) {

    override val layoutId: Int = R.layout.fragment_search_channels_list_item

    override fun bind(item: ChannelSearch, view: View) {
        with(view) {
            setOnClickListener { listener.viewChannel(item.id, item.broadcaster_login, item.display_name, item.channelLogo) }
            if (item.channelLogo != null) {
                userImage.visible()
                userImage.loadImage(fragment, item.channelLogo, circle = true)
            } else {
                userImage.gone()
            }
            if (item.display_name != null) {
                userName.visible()
                userName.text = item.display_name
            } else {
                userName.gone()
            }
            if (item.followers_count != null) {
                userFollowers.visible()
                userFollowers.text = context.getString(R.string.followers, TwitchApiHelper.formatCount(context, item.followers_count))
            } else {
                userFollowers.gone()
            }
        }
    }
}