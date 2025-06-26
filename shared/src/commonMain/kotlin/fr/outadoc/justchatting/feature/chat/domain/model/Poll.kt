package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
internal data class Poll(
    val pollId: String,
    val status: Status,
    val title: String,
    val startedAt: Instant,
    val choices: List<Choice>,
    val duration: Duration,
    val remainingDuration: Duration,
    val endedAt: Instant? = null,
    val topBitsContributor: String? = null,
    val topChannelPointsContributor: String? = null,
    val topContributor: String? = null,
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
