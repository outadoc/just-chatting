package fr.outadoc.justchatting.ui.view.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentEmotesListHeaderBinding
import fr.outadoc.justchatting.databinding.FragmentEmotesListItemBinding
import fr.outadoc.justchatting.ui.chat.EmoteSetItem
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.loadImage

class EmotesAdapter(
    private val clickListener: OnEmoteClickedListener
) : ListAdapter<EmoteSetItem, EmotesAdapter.ViewHolder>(EmoteSetItemItemCallback) {

    var animateEmotes: Boolean = true

    companion object {
        const val TYPE_EMOTE = 0
        const val TYPE_HEADER = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> ViewHolder.Header(
                FragmentEmotesListHeaderBinding.inflate(inflater, parent, false)
            )
            else -> ViewHolder.Emote(
                FragmentEmotesListItemBinding.inflate(inflater, parent, false)
            )
        }
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
        val binding = (holder as ViewHolder.Header).binding

        binding.textViewHeader.text = header.title
            ?: binding.root.context.getString(R.string.emote_category_global)
    }

    private fun bindEmote(holder: ViewHolder, position: Int) {
        val emote = (getItem(position) as? EmoteSetItem.Emote)?.emote ?: return
        val binding = (holder as ViewHolder.Emote).binding

        binding.imageViewEmote.apply {
            loadImage(
                emote.getUrl(
                    animate = animateEmotes,
                    screenDensity = context.resources.displayMetrics.density,
                    isDarkTheme = context.isDarkMode
                )
            )
            setOnClickListener {
                clickListener.onEmoteClicked(emote)
            }
        }
    }

    sealed class ViewHolder(
        val view: View
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        class Header(val binding: FragmentEmotesListHeaderBinding) : ViewHolder(binding.root)
        class Emote(val binding: FragmentEmotesListItemBinding) : ViewHolder(binding.root)
    }
}
