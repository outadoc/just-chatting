package fr.outadoc.justchatting.ui.streams

import android.view.View
import androidx.core.view.isVisible
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentStreamsListItemCompactBinding
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.util.formatNumber
import fr.outadoc.justchatting.util.formatTimestamp
import kotlinx.datetime.toInstant

class StreamsCompactAdapter(
    clickListener: NavigationHandler
) : BaseStreamsAdapter(clickListener) {

    override fun getItemViewType(position: Int): Int = R.layout.fragment_streams_list_item_compact

    override fun bind(item: Stream, view: View) {
        super.bind(item, view)

        val binding = FragmentStreamsListItemCompactBinding.bind(view)
        with(binding) {
            if (item.viewerCount != null) {
                viewers.isVisible = true
                viewers.text = item.viewerCount.formatNumber()
            } else {
                viewers.isVisible = false
            }

            if (item.startedAt != null) {
                val text = item.startedAt.toInstant().formatTimestamp(view.context)
                if (text != null) {
                    uptime.isVisible = true
                    uptime.text = text
                } else {
                    uptime.isVisible = false
                }
            } else {
                uptime.isVisible = false
            }
        }
    }
}
