package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.tagsLayout
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.type
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.uptime
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.viewers

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

            if (item.type != null) {
                val text = TwitchApiHelper.getType(context, item.type)
                if (text != null) {
                    type.isVisible = true
                    type.text = text
                } else {
                    type.isVisible = false
                }
            } else {
                type.isVisible = false
            }

            if (item.started_at != null) {
                val text = TwitchApiHelper.getUptime(context = context, input = item.started_at)
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
                tagsLayout.removeAllViews()
                tagsLayout.isVisible = true
                for (tag in item.tags) {
                    val text = TextView(context)
                    text.text = tag.name
                    tagsLayout.addView(text)
                }
            } else {
                tagsLayout.isVisible = false
            }
        }
    }
}
