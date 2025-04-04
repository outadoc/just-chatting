package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.uptime
import fr.outadoc.justchatting.shared.viewers
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import fr.outadoc.justchatting.utils.presentation.formatNumber
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StreamInfo(
    modifier: Modifier = Modifier,
    stream: Stream,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = stream.title)

        stream.category?.name
            .takeUnless { it.isNullOrEmpty() }
            ?.let { gameName ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp),
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                    )

                    Text(text = gameName)
                }
            }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
            )

            Text(
                text = pluralStringResource(
                    Res.plurals.viewers,
                    stream.viewerCount.toInt(),
                    stream.viewerCount.toInt().formatNumber(),
                ),
            )
        }

        val startedAt = stream.startedAt.formatHourMinute()
        if (startedAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Start,
                    contentDescription = null,
                )

                Text(text = stringResource(Res.string.uptime, startedAt))
            }
        }

        if (stream.tags.isNotEmpty()) {
            TagList(
                modifier = Modifier.padding(top = 4.dp),
                tags = stream.tags,
            )
        }
    }
}
