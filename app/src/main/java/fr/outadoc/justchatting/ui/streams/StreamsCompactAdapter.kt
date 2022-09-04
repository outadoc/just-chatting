package fr.outadoc.justchatting.ui.streams

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
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

    override val layoutId: Int = R.layout.fragment_streams_list_item_compact

    override fun bind(item: Stream, view: View) {
        super.bind(item, view)

        val binding = FragmentStreamsListItemCompactBinding.bind(view)
        with(binding) {
            if (item.viewer_count != null) {
                viewers.isVisible = true
                viewers.text = item.viewer_count.formatNumber()
            } else {
                viewers.isVisible = false
            }

            if (item.started_at != null) {
                val text = item.started_at.toInstant().formatTimestamp(view.context)
                if (text != null) {
                    uptime.isVisible = true
                    uptime.text = text
                } else {
                    uptime.isVisible = false
                }
            } else {
                uptime.isVisible = false
            }

            if (item.tags != null) {
                val inflater = LayoutInflater.from(view.context)
                chipGroupTagsContainer.removeAllViews()
                for (tag in item.tags) {
                    val chip = inflater.inflate(R.layout.item_single_tag, null)
                    val chipTag = chip.findViewById<TextView>(R.id.chipTag)
                    chipTag.text = tag.name
                    chipGroupTagsContainer.addView(chip)
                }
                scrollViewTagsContainer.isVisible = true
            } else {
                scrollViewTagsContainer.isVisible = false
            }
        }
    }
}
