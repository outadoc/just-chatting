package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.uptime
import fr.outadoc.justchatting.shared.internal.viewers
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import fr.outadoc.justchatting.utils.presentation.formatNumber
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StreamInfoCard(
    modifier: Modifier = Modifier,
    stream: Stream,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        StreamInfo(
            modifier = Modifier.padding(16.dp),
            stream = stream,
        )
    }
}

@Composable
internal fun StreamInfo(
    modifier: Modifier = Modifier,
    stream: Stream,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stream.title,
            style = MaterialTheme.typography.titleMedium,
        )

        stream.category
            ?.name
            .takeUnless { it.isNullOrEmpty() }
            ?.let { gameName ->
                StreamInfoRow(
                    icon = Icons.Outlined.SportsEsports,
                    text = gameName,
                )
            }

        StreamInfoRow(
            icon = Icons.Outlined.Visibility,
            text =
                pluralStringResource(
                    Res.plurals.viewers,
                    stream.viewerCount.toInt(),
                    stream.viewerCount.toInt().formatNumber(),
                ),
        )

        val startedAt = stream.startedAt.formatHourMinute()
        if (startedAt != null) {
            StreamInfoRow(
                icon = Icons.Outlined.Schedule,
                text = stringResource(Res.string.uptime, startedAt),
            )
        }

        if (stream.tags.isNotEmpty()) {
            TagList(
                modifier = Modifier.padding(top = 4.dp),
                tags = stream.tags,
            )
        }
    }
}

@Composable
internal fun StreamInfoRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
