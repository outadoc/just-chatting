package com.github.andreyasadchy.xtra.ui.view.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.chat.EmoteSetItem
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.isDarkMode
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs

class EmotesAdapter(
    private val clickListener: OnEmoteClickedListener
) : ListAdapter<EmoteSetItem, ViewHolder>(object :
    DiffUtil.ItemCallback<EmoteSetItem>() {

    override fun areItemsTheSame(oldItem: EmoteSetItem, newItem: EmoteSetItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: EmoteSetItem, newItem: EmoteSetItem): Boolean {
        return oldItem == newItem
    }
}) {

    companion object {
        const val TYPE_EMOTE = 0
        const val TYPE_HEADER = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewId = when (viewType) {
            TYPE_HEADER -> R.layout.fragment_emotes_list_header
            else -> R.layout.fragment_emotes_list_item
        }

        return object : ViewHolder(
            LayoutInflater.from(parent.context).inflate(viewId, parent, false)
        ) {}
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EmoteSetItem.Emote -> TYPE_EMOTE
            is EmoteSetItem.Header -> TYPE_HEADER
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> bindHeader(holder, position)
            TYPE_EMOTE -> bindEmote(holder, position)
        }
    }

    private fun bindHeader(holder: ViewHolder, position: Int) {
        val header = (getItem(position) as? EmoteSetItem.Header) ?: return
        val titleView: TextView = holder.itemView.findViewById(R.id.textView_header)
        titleView.text = header.title
    }

    private fun bindEmote(holder: ViewHolder, position: Int) {
        val emote = (getItem(position) as? EmoteSetItem.Emote)?.emote ?: return
        val imageView: ImageView = holder.itemView.findViewById(R.id.imageView_emote)

        imageView.apply {
            loadImage(
                context,
                emote.getUrl(
                    animate = context.prefs().getBoolean(C.ANIMATED_EMOTES, true),
                    screenDensity = context.resources.displayMetrics.density,
                    isDarkTheme = context.isDarkMode
                )
            )
            setOnClickListener {
                clickListener.onEmoteClicked(emote)
            }
        }
    }
}
