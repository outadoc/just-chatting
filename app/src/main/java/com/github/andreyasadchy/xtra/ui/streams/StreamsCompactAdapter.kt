package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.tagsLayout
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.type
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.uptime
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.viewers

class StreamsCompactAdapter(
    fragment: Fragment,
    clickListener: BaseStreamsFragment.OnStreamSelectedListener
) : BaseStreamsAdapter(fragment, clickListener) {

    override val layoutId: Int = R.layout.fragment_streams_list_item_compact

    override fun bind(item: Stream, view: View) {
        super.bind(item, view)
        with(view) {
            if (item.viewer_count != null) {
                viewers.visible()
                viewers.text = TwitchApiHelper.formatCount(context, item.viewer_count)
            } else {
                viewers.gone()
            }
            if (item.type != null) {
                val text = TwitchApiHelper.getType(context, item.type)
                if (text != null) {
                    type.visible()
                    type.text = text
                } else {
                    type.gone()
                }
            } else {
                type.gone()
            }
            if (item.started_at != null) {
                val text = TwitchApiHelper.getUptime(context = context, input = item.started_at)
                if (text != null) {
                    uptime.visible()
                    uptime.text = text
                } else {
                    uptime.gone()
                }
            } else {
                uptime.gone()
            }
            if (item.tags != null && context.prefs().getBoolean(C.UI_TAGS, true)) {
                tagsLayout.removeAllViews()
                tagsLayout.visible()
                for (tag in item.tags) {
                    val text = TextView(context)
                    text.text = tag.name
                    tagsLayout.addView(text)
                }
            } else {
                tagsLayout.gone()
            }
        }
    }
}
