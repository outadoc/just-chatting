package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Poll
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
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
                    title = choice.title,
                    votes = choice.votes.total,
                    totalVotes = poll.votes.total,
                    isWinner = choice == winningChoice,
                )
            }
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
