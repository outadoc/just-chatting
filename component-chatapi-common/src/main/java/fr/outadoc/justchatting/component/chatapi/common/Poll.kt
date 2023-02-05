package fr.outadoc.justchatting.component.chatapi.common

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Immutable
data class Poll(
    val pollId: String,
    val status: Status,
    val title: String,
    val startedAt: Instant,
    val choices: List<Choice>,
    val duration: Duration,
    val remainingDuration: Duration,
    val endedAt: Instant?,
    val topBitsContributor: String?,
    val topChannelPointsContributor: String?,
    val topContributor: String?,
    val totalVoters: Int,
    val votes: Votes,
) {

    @Immutable
    enum class Status {
        Active,
        Completed,
        Archived,
    }

    @Immutable
    data class Choice(
        val choiceId: String,
        val title: String,
        val votes: Votes,
        val totalVoters: Int,
    )

    @Immutable
    data class Votes(
        val total: Int,
        val bits: Int,
        val channelPoints: Int,
        val base: Int,
    )
}
