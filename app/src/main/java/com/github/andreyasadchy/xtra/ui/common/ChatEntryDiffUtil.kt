package com.github.andreyasadchy.xtra.ui.common

import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.ui.view.chat.model.ChatEntry

object ChatEntryDiffUtil : DiffUtil.ItemCallback<ChatEntry>() {

    override fun areItemsTheSame(oldItem: ChatEntry, newItem: ChatEntry): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ChatEntry, newItem: ChatEntry): Boolean {
        return oldItem == newItem
    }
}
