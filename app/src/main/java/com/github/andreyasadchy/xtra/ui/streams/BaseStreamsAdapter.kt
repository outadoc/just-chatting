package com.github.andreyasadchy.xtra.ui.streams

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_streams_list_item.view.*

abstract class BaseStreamsAdapter(
        protected val fragment: Fragment,
        private val clickListener: BaseStreamsFragment.OnStreamSelectedListener,
        private val channelClickListener: OnChannelSelectedListener) : BasePagedListAdapter<Stream>(
        object : DiffUtil.ItemCallback<Stream>() {
            override fun areItemsTheSame(oldItem: Stream, newItem: Stream): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Stream, newItem: Stream): Boolean =
                    oldItem.viewer_count == newItem.viewer_count &&
                            oldItem.game_name == newItem.game_name &&
                            oldItem.title == newItem.title
        }) {

    override fun bind(item: Stream, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.user_id, item.user_login, item.user_name, item.profileImageURL) }
        with(view) {
            setOnClickListener { clickListener.startStream(item) }
            userImage.apply {
                setOnClickListener(channelListener)
                loadImage(fragment, TwitchApiHelper.getTemplateUrl(item.profileImageURL, "profileimage", if (context.prefs().getBoolean(C.API_USEHELIX, true) && context.prefs().getString(C.USERNAME, "") != "") "4" else "3"), circle = true)
            }
            username.apply {
                setOnClickListener(channelListener)
                text = item.user_name
            }
            title.text = item.title.trim()
            gameName.text = item.game_name
        }
    }
}
