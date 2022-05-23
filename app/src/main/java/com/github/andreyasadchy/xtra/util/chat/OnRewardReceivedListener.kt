package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.ChatMessage

interface OnRewardReceivedListener {
    fun onReward(message: ChatMessage)
}
