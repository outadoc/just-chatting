package fr.outadoc.justchatting.ui.streams

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import fr.outadoc.justchatting.databinding.FragmentStreamsListItemCompactBinding
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.util.loadImage

abstract class BaseStreamsAdapter(
    private val clickListener: NavigationHandler
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
        view.setOnClickListener {
            item.user_login?.let { login ->
                clickListener.viewChannel(login)
            }
        }

        val binding = FragmentStreamsListItemCompactBinding.bind(view)
        with(binding) {
            if (item.channelLogo != null) {
                userImage.isVisible = true
                userImage.loadImage(item.channelLogo, circle = true)
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
