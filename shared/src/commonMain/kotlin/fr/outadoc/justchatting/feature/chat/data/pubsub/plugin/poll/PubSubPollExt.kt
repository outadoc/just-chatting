package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.poll

import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal fun PubSubPollMessage.Data.Poll.map(): Poll =
    Poll(
        pollId = pollId,
        status = status.map(),
        title = title,
        startedAt = Instant.parse(startedAtIso),
        choices = choices.map { it.map() },
        duration = durationSeconds.seconds,
        remainingDuration = remainingDurationMilliseconds.milliseconds,
        endedAt = endedAtIso?.let { Instant.parse(it) },
        topBitsContributor = topBitsContributor,
        topChannelPointsContributor = topChannelPointsContributor,
        topContributor = topContributor,
        totalVoters = totalVoters,
        votes = votes.map(),
    )

private fun PubSubPollMessage.Data.Poll.Status.map(): Poll.Status =
    when (this) {
        PubSubPollMessage.Data.Poll.Status.Active -> Poll.Status.Active
        PubSubPollMessage.Data.Poll.Status.Completed -> Poll.Status.Completed
        PubSubPollMessage.Data.Poll.Status.Archived -> Poll.Status.Archived
    }

private fun PubSubPollMessage.Data.Poll.Choice.map(): Poll.Choice =
    Poll.Choice(
        choiceId = choiceId,
        title = title,
        votes = votes.map(),
        totalVoters = totalVoters,
    )

private fun PubSubPollMessage.Data.Poll.Votes.map(): Poll.Votes =
    Poll.Votes(
        total = total,
        bits = bits,
        channelPoints = channelPoints,
        base = base,
    )
