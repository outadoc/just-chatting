package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_streams_list_item.view.*

class StreamsAdapter(
        fragment: Fragment,
        clickListener: BaseStreamsFragment.OnStreamSelectedListener,
        channelClickListener: OnChannelSelectedListener,
        viewcount: Boolean) : BaseStreamsAdapter(fragment, clickListener, channelClickListener) {

    val viewcount2 = viewcount
    override val layoutId: Int = R.layout.fragment_streams_list_item

    override fun bind(item: Stream, view: View) {
        super.bind(item, view)
        with(view) {
            thumbnail.loadImage(fragment, item.preview.large, true, diskCacheStrategy = DiskCacheStrategy.NONE)
            viewers.text = TwitchApiHelper.formatViewersCount(context, item.viewers, viewcount2)
        }
    }
}