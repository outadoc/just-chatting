package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
data class RoomState(
    val isEmoteOnly: Boolean = false,
    val minFollowDuration: Duration = -Duration.INFINITE,
    val uniqueMessagesOnly: Boolean = false,
    val slowModeDuration: Duration = Duration.ZERO,
    val isSubOnly: Boolean = false,
) {
    companion object {
        val Default = RoomState()
    }
}
