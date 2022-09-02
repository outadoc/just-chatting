package fr.outadoc.justchatting.ui.search.channels

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentSearchChannelsListItemBinding
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.util.formatNumber
import fr.outadoc.justchatting.util.loadImage

class ChannelSearchAdapter(
    private val listener: NavigationHandler
) : BasePagedListAdapter<ChannelSearch>(

    object : DiffUtil.ItemCallback<ChannelSearch>() {
        override fun areItemsTheSame(oldItem: ChannelSearch, newItem: ChannelSearch): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChannelSearch, newItem: ChannelSearch): Boolean =
            true
    }) {

    override val layoutId: Int = R.layout.fragment_search_channels_list_item

    override fun bind(item: ChannelSearch, view: View) {
        view.setOnClickListener {
            listener.viewChannel(
                item.id,
                item.broadcaster_login,
                item.display_name,
                item.channelLogo
            )
        }

        val binding = FragmentSearchChannelsListItemBinding.bind(view)
        with(binding) {
            if (item.channelLogo != null) {
                userImage.isVisible = true
                userImage.loadImage(item.channelLogo, circle = true)
            } else {
                userImage.isVisible = false
            }

            if (item.display_name != null) {
                userName.isVisible = true
                userName.text = item.display_name
            } else {
                userName.isVisible = false
            }

            if (item.followers_count != null) {
                userFollowers.isVisible = true
                userFollowers.text = view.context.getString(
                    R.string.followers,
                    item.followers_count.formatNumber()
                )
            } else {
                userFollowers.isVisible = false
            }
        }
    }
}
