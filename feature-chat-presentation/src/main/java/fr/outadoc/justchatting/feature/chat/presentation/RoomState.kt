package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
data class RoomState(
    val isEmoteOnly: Boolean = false,
    val minFollowDuration: Duration? = null,
    val uniqueMessagesOnly: Boolean = false,
    val slowModeDuration: Duration? = null,
    val isSubOnly: Boolean = false
) {
    val isDefault: Boolean =
        !isEmoteOnly &&
            !uniqueMessagesOnly &&
            !isSubOnly &&
            minFollowDuration == null &&
            slowModeDuration == null
}