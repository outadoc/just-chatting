package fr.outadoc.justchatting.ui.view.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fr.outadoc.justchatting.databinding.AutoCompleteEmotesListItemBinding
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.loadImage

class AutoCompleteAdapter(context: Context) : ArrayAdapter<AutoCompleteItem>(context, 0) {

    var animateEmotes: Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) ?: error("Invalid item id")
        val inflater = LayoutInflater.from(context)

        val viewHolder = when (getItemViewType(position)) {
            TYPE_EMOTE -> {
                val viewHolder = convertView?.tag as? ViewHolder.Emote
                    ?: ViewHolder.Emote(
                        AutoCompleteEmotesListItemBinding.inflate(inflater, parent, false)
                    )

                item as AutoCompleteItem.EmoteItem
                viewHolder.apply {
                    root.tag = this
                    binding.image.loadImage(
                        url = item.emote.getUrl(
                            animate = animateEmotes,
                            screenDensity = context.resources.displayMetrics.density,
                            isDarkTheme = context.isDarkMode
                        )
                    )
                    binding.name.text = item.emote.name
                }
            }
            TYPE_USERNAME -> {
                val viewHolder = convertView?.tag as? ViewHolder.Username
                    ?: ViewHolder.Username(
                        LayoutInflater.from(context).inflate(
                            android.R.layout.simple_list_item_1,
                            parent,
                            false
                        )
                    )

                item as AutoCompleteItem.UserItem
                viewHolder.apply {
                    root.tag = this
                    username.text = item.chatter.name
                }
            }
            else -> error("Invalid item type")
        }

        return viewHolder.root
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is AutoCompleteItem.EmoteItem -> TYPE_EMOTE
            is AutoCompleteItem.UserItem -> TYPE_USERNAME
            null -> error("Invalid item id")
        }

    override fun getViewTypeCount(): Int = 2

    sealed class ViewHolder {
        abstract val root: View

        class Username(override val root: View) : ViewHolder() {
            val username: TextView = root.findViewById(android.R.id.text1)
        }

        class Emote(val binding: AutoCompleteEmotesListItemBinding) : ViewHolder() {
            override val root: View = binding.root
        }
    }

    private companion object {
        const val TYPE_EMOTE = 0
        const val TYPE_USERNAME = 1
    }
}
