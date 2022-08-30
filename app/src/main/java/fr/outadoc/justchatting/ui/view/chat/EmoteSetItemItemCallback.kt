package fr.outadoc.justchatting.ui.view.chat

import androidx.recyclerview.widget.DiffUtil
import fr.outadoc.justchatting.ui.chat.EmoteSetItem

object EmoteSetItemItemCallback : DiffUtil.ItemCallback<EmoteSetItem>() {

    override fun areItemsTheSame(oldItem: EmoteSetItem, newItem: EmoteSetItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: EmoteSetItem, newItem: EmoteSetItem): Boolean {
        return oldItem == newItem
    }
}
