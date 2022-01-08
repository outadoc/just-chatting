package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.fragment_streams_list_item.view.*

class StreamsAdapter(
        fragment: Fragment,
        clickListener: BaseStreamsFragment.OnStreamSelectedListener,
        channelClickListener: OnChannelSelectedListener,
        gameClickListener: GamesFragment.OnGameSelectedListener) : BaseStreamsAdapter(fragment, clickListener, channelClickListener, gameClickListener) {

    override val layoutId: Int = R.layout.fragment_streams_list_item

    override fun bind(item: Stream, view: View) {
        super.bind(item, view)
        with(view) {
            thumbnail.loadImage(fragment, item.thumbnail, true, diskCacheStrategy = DiskCacheStrategy.NONE)
            if (item.viewer_count != null)  {
                viewers.visible()
                viewers.text = TwitchApiHelper.formatViewersCount(context, item.viewer_count, context.prefs().getBoolean(C.UI_TRUNCATEVIEWCOUNT, false))
            }
            if (item.type != null)  {
                type.visible()
                type.text = TwitchApiHelper.getType(context, item.type)
            }
        }
    }
}