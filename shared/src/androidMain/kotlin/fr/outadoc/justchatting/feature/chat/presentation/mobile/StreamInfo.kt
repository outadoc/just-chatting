package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.uptime
import fr.outadoc.justchatting.shared.viewers
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.formatTimestamp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StreamInfo(
    modifier: Modifier = Modifier,
    stream: Stream,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = stream.title)

        stream.gameName
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
                    stream.viewerCount,
                    stream.viewerCount.formatNumber(),
                ),
            )
        }

        val startedAt = Instant.parse(stream.startedAt).formatTimestamp()
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
