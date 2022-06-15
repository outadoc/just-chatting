package com.github.andreyasadchy.xtra.ui.streams

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.formatTimestamp
import com.github.andreyasadchy.xtra.util.prefs
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.chipGroupTagsContainer
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.scrollViewTagsContainer
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.uptime
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.viewers
import kotlinx.android.synthetic.main.item_single_tag.view.chipTag
import kotlinx.datetime.Instant

class StreamsCompactAdapter(
    clickListener: BaseStreamsFragment.OnStreamSelectedListener
) : BaseStreamsAdapter(clickListener) {

    override val layoutId: Int = R.layout.fragment_streams_list_item_compact

    override fun bind(item: Stream, view: View) {
        super.bind(item, view)
        with(view) {
            if (item.viewer_count != null) {
                viewers.isVisible = true
                viewers.text = TwitchApiHelper.formatCount(item.viewer_count)
            } else {
                viewers.isVisible = false
            }

            if (item.started_at != null) {
                val text = Instant.parse(item.started_at).formatTimestamp(context)
                if (text != null) {
                    uptime.isVisible = true
                    uptime.text = text
                } else {
                    uptime.isVisible = false
                }
            } else {
                uptime.isVisible = false
            }

            if (item.tags != null && context.prefs().getBoolean(C.UI_TAGS, true)) {
                val inflater = LayoutInflater.from(context)
                chipGroupTagsContainer.removeAllViews()
                for (tag in item.tags) {
                    val chip = inflater.inflate(R.layout.item_single_tag, null)
                    chip.chipTag.text = tag.name
                    chipGroupTagsContainer.addView(chip)
                }
                scrollViewTagsContainer.isVisible = true
            } else {
                scrollViewTagsContainer.isVisible = false
            }
        }
    }
}
