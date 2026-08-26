package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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
                    votes = Poll.Votes(total = 12345, bits = 123, channelPoints = 50, base = 1412),
                    totalVoters = 1000,
                ),
                Poll.Choice(
                    choiceId = "1",
                    title = "AntoineDaniel",
                    votes = Poll.Votes(total = 102345, bits = 123, channelPoints = 50, base = 1412),
                    totalVoters = 1000,
                ),
                Poll.Choice(
                    choiceId = "1",
                    title = "HortyUnderscore",
                    votes = Poll.Votes(total = 52450, bits = 123, channelPoints = 50, base = 1412),
                    totalVoters = 1000,
                ),
            ),
        duration = 3.minutes,
        remainingDuration = 53.seconds,
        totalVoters = 133143,
        votes = Poll.Votes(total = 134356, bits = 1311, channelPoints = 2345, base = 757),
    )

@PreviewTest
@Preview
@Composable
internal fun PollCardScreenshotTest() {
    AppTheme {
        PollCard(
            poll = mockPoll.copy(status = Poll.Status.Active),
        )
    }
}

@PreviewTest
@Preview
@Composable
internal fun PollCardCompletedScreenshotTest() {
    AppTheme {
        PollCard(
            poll = mockPoll.copy(status = Poll.Status.Completed),
        )
    }
}
