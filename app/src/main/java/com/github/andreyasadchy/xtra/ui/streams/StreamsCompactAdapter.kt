package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.ui.streams.common.StreamsFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_streams_list_item_compact.view.*

class StreamsCompactAdapter(
        fragment: Fragment,
        clickListener: BaseStreamsFragment.OnStreamSelectedListener,
        channelClickListener: OnChannelSelectedListener,
        gameClickListener: GamesFragment.OnGameSelectedListener) : BaseStreamsAdapter(fragment, clickListener, channelClickListener, gameClickListener) {

    override val layoutId: Int = R.layout.fragment_streams_list_item_compact

    override fun bind(item: Stream, view: View) {
        super.bind(item, view)
        with(view) {
            if (item.viewer_count != null) {
                viewers.visible()
                viewers.text = TwitchApiHelper.formatCount(item.viewer_count, context.prefs().getBoolean(C.UI_TRUNCATEVIEWCOUNT, false))
            }
            TwitchApiHelper.getType(context, item.type).let {
                if (it != null)  {
                    type.visible()
                    type.text = it
                }
            }
            if (context.prefs().getBoolean(C.UI_UPTIME, true)) {
                TwitchApiHelper.getUptime(context = context, input = item.started_at, noText = true).let {
                    if (it != null)  {
                        uptime.visible()
                        uptime.text = it
                    }
                }
            }
            if (item.tags != null) {
                tagsLayout.removeAllViews()
                tagsLayout.visible()
                for (tag in item.tags) {
                    val text = TextView(context)
                    text.text = tag.name
                    if (tag.id != null) {
                        text.setOnClickListener { fragment.parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, StreamsFragment.newInstance(tags = listOf(tag.id), gameId = fragment.parentFragment?.arguments?.getString(C.GAME_ID), gameName = fragment.parentFragment?.arguments?.getString(C.GAME_NAME))).commit() }
                    }
                    tagsLayout.addView(text)
                }
            }
        }
    }
}