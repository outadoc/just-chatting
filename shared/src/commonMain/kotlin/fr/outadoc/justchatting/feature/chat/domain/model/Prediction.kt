package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
public data class Prediction(
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
    public enum class Status { Active, Locked, ResolvePending, Resolved }

    @Immutable
    public data class Outcome(
        val id: String,
        val title: String,
        val color: String,
        val totalPoints: Int,
        val totalUsers: Int,
        val badge: Badge,
    )
}
