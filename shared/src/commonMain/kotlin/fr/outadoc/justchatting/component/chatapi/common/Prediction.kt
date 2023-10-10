package fr.outadoc.justchatting.component.chatapi.common

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Immutable
data class Prediction(
    val id: String,
    val title: String,
    val status: Status,
    val createdAt: Instant,
    val endedAt: Instant? = null,
    val lockedAt: Instant? = null,
    val outcomes: List<Outcome>,
    val predictionWindow: Duration,
    val winningOutcome: Outcome? = null,
) {
    @Immutable
    enum class Status { Active, Locked, ResolvePending, Resolved }

    @Immutable
    data class Outcome(
        val id: String,
        val title: String,
        val color: String,
        val totalPoints: Int,
        val totalUsers: Int,
        val badge: Badge,
    )
}
