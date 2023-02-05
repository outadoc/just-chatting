package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Poll
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.core.formatPercent
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.customColors
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun PollCard(
    modifier: Modifier = Modifier,
    poll: Poll,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            val winningChoice: Poll.Choice? =
                if (poll.status == Poll.Status.Completed) {
                    poll.choices.maxBy { choice -> choice.votes.total }
                } else {
                    null
                }

            val status = when (poll.status) {
                Poll.Status.Active -> R.string.poll_status_progress
                Poll.Status.Completed, Poll.Status.Archived -> R.string.poll_status_ended
            }

            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = buildString {
                    append(stringResource(status))
                    append(" · ")
                    append(
                        stringResource(
                            R.string.poll_status_voterCount,
                            poll.totalVoters.formatNumber(),
                        ),
                    )
                },
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = poll.title,
                style = MaterialTheme.typography.headlineSmall,
            )

            poll.choices.forEach { choice ->
                PollChoice(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    choice = choice,
                    totalPollVotes = poll.votes,
                    isWinner = choice == winningChoice,
                )
            }
        }
    }
}

@Composable
fun PollChoice(
    modifier: Modifier = Modifier,
    choice: Poll.Choice,
    totalPollVotes: Poll.Votes,
    isWinner: Boolean,
) {
    val totalVotes = totalPollVotes.total.toFloat()
    val optionVotes = choice.votes.total.toFloat()

    val ratio = if (totalVotes == 0f) 0f else (optionVotes / totalVotes)

    Box(modifier = modifier.height(32.dp)) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
            color = if (isWinner) {
                MaterialTheme.customColors.success
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            progress = ratio,
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isWinner) {
                    Icon(
                        modifier = Modifier.padding(end = 8.dp),
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.poll_status_winner_cd),
                    )
                }

                Text(
                    text = choice.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = ratio.formatPercent(),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private val mockPoll = Poll(
    pollId = "1234",
    status = Poll.Status.Completed,
    title = "Who wants to be a millionnaire?",
    startedAt = Instant.parse("2023-02-05T18:11:52.832Z"),
    choices = listOf(
        Poll.Choice(
            choiceId = "1",
            title = "Étoiles",
            votes = Poll.Votes(
                total = 12345,
                bits = 123,
                channelPoints = 50,
                base = 1412,
            ),
            totalVoters = 1000,
        ),
        Poll.Choice(
            choiceId = "1",
            title = "AntoineDaniel",
            votes = Poll.Votes(
                total = 102345,
                bits = 123,
                channelPoints = 50,
                base = 1412,
            ),
            totalVoters = 1000,
        ),
        Poll.Choice(
            choiceId = "1",
            title = "HortyUnderscore",
            votes = Poll.Votes(
                total = 52450,
                bits = 123,
                channelPoints = 50,
                base = 1412,
            ),
            totalVoters = 1000,
        ),
    ),
    duration = 3.minutes,
    remainingDuration = 53.seconds,
    totalVoters = 133143,
    votes = Poll.Votes(
        total = 134356,
        bits = 1311,
        channelPoints = 2345,
        base = 757,
    ),
)

@ThemePreviews
@Composable
fun PollCardPreview() {
    AppTheme {
        PollCard(
            poll = mockPoll.copy(status = Poll.Status.Active),
        )
    }
}

@ThemePreviews
@Composable
fun PollCardPreviewCompleted() {
    AppTheme {
        PollCard(
            poll = mockPoll.copy(status = Poll.Status.Completed),
        )
    }
}
