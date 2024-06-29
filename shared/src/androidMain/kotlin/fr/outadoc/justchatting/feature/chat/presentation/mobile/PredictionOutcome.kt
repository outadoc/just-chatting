package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.utils.core.formatPercent
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.ThemePreviews

@Composable
internal fun PredictionOutcome(
    modifier: Modifier = Modifier,
    title: String,
    votes: Int,
    totalVotes: Int,
    color: Color,
    icon: @Composable () -> Unit = {},
) {
    val ratio: Float =
        if (totalVotes == 0) {
            0f
        } else {
            (votes.toFloat() / totalVotes.toFloat())
        }

    CompositionLocalProvider(
        LocalContentColor provides color,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            icon()

            Column(
                modifier = Modifier
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .height(8.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    progress = ratio,
                )
            }

            Text(
                text = ratio.formatPercent(),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@ThemePreviews
@Composable
internal fun PredictionOutcomePreview() {
    AppTheme {
        PredictionOutcome(
            title = "Antoine",
            votes = 123,
            totalVotes = 300,
            color = Color.Red,
            icon = {},
        )
    }
}

@ThemePreviews
@Composable
internal fun PredictionOutcomePreviewWinning() {
    AppTheme {
        PredictionOutcome(
            modifier = Modifier.width(800.dp),
            title = "Baghera",
            votes = 123,
            totalVotes = 300,
            color = Color.Blue,
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                )
            },
        )
    }
}
