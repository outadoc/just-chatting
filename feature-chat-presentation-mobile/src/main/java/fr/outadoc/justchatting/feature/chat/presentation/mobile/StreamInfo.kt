package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.formatTime
import fr.outadoc.justchatting.utils.ui.formatTimestamp
import kotlinx.datetime.toInstant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StreamInfo(
    modifier: Modifier = Modifier,
    user: User?,
    stream: Stream?,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stream?.title?.let { title ->
            Text(text = title)
        }

        val tags = stream?.tags
        if (!tags.isNullOrEmpty()) {
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

        stream?.viewerCount?.let { viewerCount ->
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
                        R.plurals.viewers,
                        viewerCount,
                        viewerCount.formatNumber(),
                    ),
                )
            }
        }

        val startedAt = stream?.startedAt?.toInstant()?.formatTimestamp()
        if (startedAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Start,
                    contentDescription = null,
                )
                Text(text = stringResource(R.string.uptime, startedAt))
            }
        }

        val createdAt = user?.createdAt?.toInstant()?.formatTime()
        if (createdAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Cake,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.created_at, createdAt),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun StreamInfoPreviewFull() {
    AppTheme {
        StreamInfo(
            user = User(
                id = "",
                login = "",
                displayName = "",
                createdAt = "2022-01-01T00:00:00.00Z",
            ),
            stream = Stream(
                title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
                gameName = "",
                startedAt = "2022-09-01T00:00:00.00Z",
                viewerCount = 10_000,
                tags = listOf(
                    "French",
                ),
            ),
        )
    }
}

@ThemePreviews
@Composable
fun StreamInfoPreviewOffline() {
    AppTheme {
        StreamInfo(
            user = User(
                id = "",
                login = "",
                displayName = "",
                createdAt = "2022-01-01T00:00:00.00Z",
            ),
            stream = null,
        )
    }
}
