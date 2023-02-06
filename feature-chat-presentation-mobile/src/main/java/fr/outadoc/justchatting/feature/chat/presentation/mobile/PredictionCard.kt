package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.Prediction
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.ensureColorIsAccessible
import fr.outadoc.justchatting.utils.ui.parseHexColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

@Composable
fun PredictionCard(
    modifier: Modifier = Modifier,
    prediction: Prediction,
    badges: ImmutableList<TwitchBadge> = persistentListOf(),
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            val status = when (prediction.status) {
                Prediction.Status.Active -> R.string.prediction_status_progress
                Prediction.Status.Locked -> R.string.prediction_status_locked
                Prediction.Status.ResolvePending,
                Prediction.Status.Resolved,
                -> R.string.prediction_status_ended
            }

            val totalPointsSpent: Int =
                prediction.outcomes.sumOf { outcome -> outcome.totalPoints }

            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = buildString {
                    append(stringResource(status))
                    append(" · ")
                    append(
                        stringResource(
                            R.string.prediction_status_points,
                            totalPointsSpent.formatNumber(),
                        ),
                    )
                },
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = prediction.title,
                style = MaterialTheme.typography.headlineSmall,
            )

            prediction.outcomes.forEach { outcome ->
                PredictionOutcome(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    title = outcome.title,
                    votes = outcome.totalPoints,
                    totalVotes = totalPointsSpent,
                    color = ensureColorIsAccessible(
                        foreground = outcome.color.parseHexColor(),
                        background = MaterialTheme.colorScheme.surface,
                    ) ?: LocalContentColor.current,
                    icon = {
                        badges.firstOrNull { badge ->
                            badge.id == outcome.badge.id &&
                                badge.version == outcome.badge.version
                        }?.let { twitchBadge ->
                            BadgeItem(
                                modifier = Modifier.size(24.dp),
                                badge = twitchBadge,
                            )
                        }
                    },
                )
            }
        }
    }
}

private val mockPrediction = Prediction(
    id = "1234",
    status = Prediction.Status.Resolved,
    title = "QUI GAGNE ?",
    createdAt = Instant.parse("2023-02-05T18:11:52.832Z"),
    predictionWindow = 5.minutes,
    outcomes = listOf(
        Prediction.Outcome(
            id = "1",
            title = "Étoiles",
            totalPoints = 12345,
            totalUsers = 1000,
            badge = Badge(
                id = "123",
                version = "5",
            ),
            color = "#00FF00",
        ),
        Prediction.Outcome(
            id = "1",
            title = "AntoineDaniel",
            totalPoints = 102345,
            totalUsers = 1000,
            badge = Badge(
                id = "123",
                version = "5",
            ),
            color = "#FF0000",
        ),
        Prediction.Outcome(
            id = "1",
            title = "HortyUnderscore",
            totalPoints = 52450,
            totalUsers = 1000,
            badge = Badge(
                id = "123",
                version = "5",
            ),
            color = "#0000FF",
        ),
    ),
)

@ThemePreviews
@Composable
fun PredictionCardPreview() {
    AppTheme {
        PredictionCard(
            prediction = mockPrediction.copy(status = Prediction.Status.Active),
        )
    }
}

@ThemePreviews
@Composable
fun PredictionCardPreviewCompleted() {
    AppTheme {
        PredictionCard(
            prediction = mockPrediction.copy(status = Prediction.Status.Resolved),
        )
    }
}
