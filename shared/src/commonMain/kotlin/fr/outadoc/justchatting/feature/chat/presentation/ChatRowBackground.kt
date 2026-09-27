package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.utils.core.isEven

/**
 * Chat rows alternate between two backgrounds. For a message to keep its background as new
 * messages arrive, the parity of its position counted from the oldest message must never change,
 * which is why [ChatStateReducer] only ever trims an even number of messages from the list.
 */
public object ChatRowBackground {
    /**
     * Whether the message at [index] in `chatMessages` (newest first), which holds [messageCount]
     * messages, uses the alternate background.
     */
    public fun isAlternate(
        index: Int,
        messageCount: Int,
    ): Boolean = (messageCount - index).isEven
}
