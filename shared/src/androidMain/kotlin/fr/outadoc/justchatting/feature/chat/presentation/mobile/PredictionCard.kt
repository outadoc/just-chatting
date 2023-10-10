package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.Prediction
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.ensureColorIsAccessible
import fr.outadoc.justchatting.utils.ui.parseHexColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionCard(
    modifier: Modifier = Modifier,
    prediction: Prediction,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    badges: ImmutableList<TwitchBadge> = persistentListOf(),
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        onClick = { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = color,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val status = when (prediction.status) {
                Prediction.Status.Active -> MR.strings.prediction_status_progress
                Prediction.Status.Locked -> MR.strings.prediction_status_locked
                Prediction.Status.ResolvePending,
                Prediction.Status.Resolved,
                -> MR.strings.prediction_status_ended
            }

            val totalPointsSpent: Int =
                prediction.outcomes.sumOf { outcome -> outcome.totalPoints }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = true),
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = buildString {
                            append(stringResource(status))
                            append(" · ")
                            append(
                                stringResource(
                                    MR.strings.prediction_status_points,
                                    totalPointsSpent.formatNumber(),
                                ),
                            )
                        },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = prediction.title,
                        style = MaterialTheme.typography.titleLarge,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isExpanded) {
                    Icon(
                        Icons.Default.ArrowDropUp,
                        contentDescription = stringResource(MR.strings.prediction_collapse_action),
                    )
                } else {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(MR.strings.prediction_expand_action),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    prediction.outcomes.forEach { outcome ->
                        PredictionOutcome(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            title = outcome.title,
                            votes = outcome.totalPoints,
                            totalVotes = totalPointsSpent,
                            color = outcome.color.parseHexColor()
                                ?.let { color ->
                                    ensureColorIsAccessible(
                                        foreground = color,
                                        background = MaterialTheme.colorScheme.surface,
                                    )
                                }
                                ?: LocalContentColor.current,
                            icon = {
                                badges
                                    .firstOrNull { badge ->
                                        badge.setId == outcome.badge.id && badge.version == outcome.badge.version
                                    }
                                    ?.let { twitchBadge ->
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
