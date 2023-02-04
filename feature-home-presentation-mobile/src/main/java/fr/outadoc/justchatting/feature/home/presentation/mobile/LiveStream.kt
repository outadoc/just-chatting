package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.formatTimestamp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

@ThemePreviews
@Composable
fun LiveStreamPreview() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier.padding(8.dp),
            userName = "Maghla",
            gameName = "Just Chatting",
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = "2022-01-01T13:45:04.00Z".toInstant(),
            profileImageURL = null,
            tags = listOf(
                "French",
                "Test",
                "Sponsored",
                "Label 1",
                "Super long label with too much text, you can't really argue otherwise",
            ),
        )
    }
}

@ThemePreviews
@Composable
fun LiveStreamLongPreview() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier
                .width(250.dp)
                .padding(8.dp),
            userName = "Maghla",
            gameName = "The Dark Pictures Anthology: The Devil in Me",
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = "2022-01-01T13:45:04.00Z".toInstant(),
            profileImageURL = null,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    userName: String? = null,
    viewerCount: Int? = null,
    gameName: String? = null,
    startedAt: Instant? = null,
    profileImageURL: String? = null,
    tags: List<String> = persistentListOf(),
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        onClick = onClick,
    ) {
        LiveStream(
            modifier = Modifier.padding(8.dp),
            title = title,
            userName = userName,
            viewerCount = viewerCount,
            gameName = gameName,
            startedAt = startedAt,
            profileImageURL = profileImageURL,
            tags = tags,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveStream(
    modifier: Modifier = Modifier,
    title: String?,
    userName: String?,
    viewerCount: Int?,
    gameName: String?,
    startedAt: Instant?,
    profileImageURL: String?,
    tags: List<String>,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(56.dp)
                .clip(CircleShape),
            model = profileImageURL,
            contentDescription = null,
        )

        Column {
            title?.let { title ->
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                userName?.let { userName ->
                    Text(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .alignByBaseline(),
                        text = userName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                viewerCount?.let { viewerCount ->
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = viewerCount.formatNumber(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                gameName?.let { gameName ->
                    Text(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .alignByBaseline(),
                        text = gameName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                startedAt
                    ?.formatTimestamp()
                    ?.let { startedAt ->
                        Text(
                            modifier = Modifier.alignByBaseline(),
                            text = startedAt,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
            }

            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tags.forEach { tag ->
                        StreamTagChip(tag = tag)
                    }
                }
            }
        }
    }
}
