package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
public data class RoomState(
    val isEmoteOnly: Boolean = false,
    val minFollowDuration: Duration = (-1).minutes,
    val uniqueMessagesOnly: Boolean = false,
    val slowModeDuration: Duration = Duration.ZERO,
    val isSubOnly: Boolean = false,
) {
    public companion object {
        public val Default: RoomState = RoomState()
    }
}
