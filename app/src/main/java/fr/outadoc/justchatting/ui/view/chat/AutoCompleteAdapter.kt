package fr.outadoc.justchatting.ui.view.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.loadImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.image
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.name

class AutoCompleteAdapter(context: Context) : ArrayAdapter<AutoCompleteItem>(context, 0) {

    var animateEmotes: Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) ?: error("Invalid item id")
        val viewHolder = when (getItemViewType(position)) {
            TYPE_EMOTE -> {
                val viewHolder = convertView?.tag as? ViewHolder
                    ?: ViewHolder(
                        LayoutInflater.from(context).inflate(
                            R.layout.auto_complete_emotes_list_item,
                            parent,
                            false
                        )
                    )

                item as AutoCompleteItem.EmoteItem
                viewHolder.apply {
                    containerView.tag = this
                    containerView.image.loadImage(
                        context = context,
                        url = item.emote.getUrl(
                            animate = animateEmotes,
                            screenDensity = context.resources.displayMetrics.density,
                            isDarkTheme = context.isDarkMode
                        )
                    )
                    containerView.name.text = item.emote.name
                }
            }
            TYPE_USERNAME -> {
                val viewHolder = convertView?.tag as? ViewHolder
                    ?: ViewHolder(
                        LayoutInflater.from(context).inflate(
                            android.R.layout.simple_list_item_1,
                            parent,
                            false
                        )
                    )

                item as AutoCompleteItem.UserItem
                viewHolder.apply {
                    containerView.tag = this
                    (containerView as TextView).text = item.chatter.name
                }
            }
            else -> error("Invalid item type")
        }

        return viewHolder.containerView
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is AutoCompleteItem.EmoteItem -> TYPE_EMOTE
            is AutoCompleteItem.UserItem -> TYPE_USERNAME
            null -> error("Invalid item id")
        }

    override fun getViewTypeCount(): Int = 2

    class ViewHolder(override val containerView: View) : LayoutContainer

    private companion object {
        const val TYPE_EMOTE = 0
        const val TYPE_USERNAME = 1
    }
}