package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.poll_status_winner_cd
import fr.outadoc.justchatting.utils.presentation.customColors
import fr.outadoc.justchatting.utils.presentation.formatPercent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PollChoice(
    modifier: Modifier = Modifier,
    title: String,
    votes: Int,
    totalVotes: Int,
    isWinner: Boolean,
) {
    val ratio: Float =
        if (totalVotes == 0) {
            0f
        } else {
            (votes.toFloat() / totalVotes.toFloat())
        }

    Box(modifier = modifier.height(32.dp)) {
        LinearProgressIndicator(
            progress = { ratio },
            modifier =
            Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
            color =
            if (isWinner) {
                MaterialTheme.customColors.success
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            trackColor = MaterialTheme.colorScheme.outlineVariant,
        )

        Row(
            modifier =
            Modifier
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
                        contentDescription = stringResource(Res.string.poll_status_winner_cd),
                    )
                }

                Text(
                    text = title,
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
