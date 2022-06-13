package com.github.andreyasadchy.xtra.ui.view.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.isDarkMode
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs

class EmotesAdapter(
    private val clickListener: OnEmoteClickedListener
) : ListAdapter<Emote, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Emote>() {

    override fun areItemsTheSame(oldItem: Emote, newItem: Emote): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Emote, newItem: Emote): Boolean {
        return true
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_emotes_list_item, parent, false)
        ) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val emote = getItem(position)
        (holder.itemView as ImageView).apply {
            loadImage(
                context,
                emote.getUrl(
                    animate = context.prefs().getBoolean(C.ANIMATED_EMOTES, true),
                    screenDensity = context.resources.displayMetrics.density,
                    isDarkTheme = context.isDarkMode
                ),
                diskCacheStrategy = DiskCacheStrategy.DATA
            )
            setOnClickListener { clickListener.onEmoteClicked(emote) }
        }
    }
}
