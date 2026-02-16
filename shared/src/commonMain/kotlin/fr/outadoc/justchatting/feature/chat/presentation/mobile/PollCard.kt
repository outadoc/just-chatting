package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.poll_collapse_action
import fr.outadoc.justchatting.shared.poll_expand_action
import fr.outadoc.justchatting.shared.poll_status_ended
import fr.outadoc.justchatting.shared.poll_status_progress
import fr.outadoc.justchatting.shared.poll_status_voterCount
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatNumber
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
internal fun PollCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    poll: Poll,
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        onClick = { isExpanded = !isExpanded },
        colors =
        CardDefaults.cardColors(
            containerColor = color,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val winningChoice: Poll.Choice? =
                if (poll.status == Poll.Status.Completed) {
                    poll.choices.maxBy { choice -> choice.votes.total }
                } else {
                    null
                }

            val status =
                when (poll.status) {
                    Poll.Status.Active -> Res.string.poll_status_progress
                    Poll.Status.Completed, Poll.Status.Archived -> Res.string.poll_status_ended
                }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = true),
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text =
                        buildString {
                            append(stringResource(status))
                            append(" · ")
                            append(
                                stringResource(
                                    Res.string.poll_status_voterCount,
                                    poll.totalVoters.formatNumber(),
                                ),
                            )
                        },
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Text(
                        text = poll.title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                if (isExpanded) {
                    Icon(
                        Icons.Default.ArrowDropUp,
                        contentDescription = stringResource(Res.string.poll_collapse_action),
                    )
                } else {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(Res.string.poll_expand_action),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    poll.choices.forEach { choice ->
                        PollChoice(
                            modifier =
                            Modifier
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
    }
}

private val mockPoll =
    Poll(
        pollId = "1234",
        status = Poll.Status.Completed,
        title = "Who wants to be a millionnaire?",
        startedAt = Instant.parse("2023-02-05T18:11:52.832Z"),
        choices =
        listOf(
            Poll.Choice(
                choiceId = "1",
                title = "Étoiles",
                votes =
                Poll.Votes(
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
                votes =
                Poll.Votes(
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
                votes =
                Poll.Votes(
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
        votes =
        Poll.Votes(
            total = 134356,
            bits = 1311,
            channelPoints = 2345,
            base = 757,
        ),
    )

@Preview
@Composable
internal fun PollCardPreview() {
    AppTheme {
        PollCard(
            poll = mockPoll.copy(status = Poll.Status.Active),
        )
    }
}

@Preview
@Composable
internal fun PollCardPreviewCompleted() {
    AppTheme {
        PollCard(
            poll = mockPoll.copy(status = Poll.Status.Completed),
        )
    }
}
