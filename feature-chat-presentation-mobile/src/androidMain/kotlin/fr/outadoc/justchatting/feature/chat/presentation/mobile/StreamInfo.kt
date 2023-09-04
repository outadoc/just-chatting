package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.formatTimestamp
import kotlinx.datetime.toInstant

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
                    fr.outadoc.justchatting.component.twitch.R.plurals.viewers,
                    stream.viewerCount,
                    stream.viewerCount.formatNumber(),
                ),
            )
        }

        val startedAt = stream.startedAt.toInstant().formatTimestamp()
        if (startedAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Start,
                    contentDescription = null,
                )

                Text(text = stringResource(MR.strings.uptime, startedAt))
            }
        }

        val tags = stream.tags
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tags.forEach { tag ->
                    StreamTagChip(
                        modifier = Modifier.padding(vertical = 2.dp),
                        tag = tag,
                    )
                }
            }
        }
    }
}
