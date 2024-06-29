package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.outadoc.justchatting.feature.chat.presentation.mobile.TagList
import fr.outadoc.justchatting.feature.chat.presentation.mobile.remoteImageModel
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.ThemePreviews
import fr.outadoc.justchatting.utils.presentation.formatNumber
import fr.outadoc.justchatting.utils.presentation.formatTimestamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant

@ThemePreviews
@Composable
internal fun LiveStreamPreview() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier.padding(8.dp),
            userName = "Maghla",
            gameName = "Just Chatting",
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageURL = null,
            tags = persistentListOf(
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
internal fun LiveStreamLongPreview() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier
                .width(250.dp)
                .padding(8.dp),
            userName = "Maghla",
            gameName = "The Dark Pictures Anthology: The Devil in Me",
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageURL = null,
        )
    }
}

@Composable
internal fun LiveStreamCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    userName: String? = null,
    viewerCount: Int? = null,
    gameName: String? = null,
    startedAt: Instant? = null,
    profileImageURL: String? = null,
    tags: ImmutableList<String> = persistentListOf(),
    onClick: () -> Unit = {},
) {
    OutlinedCard(
        modifier = modifier,
    ) {
        Column {
            Card(
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
                )
            }

            if (tags.isNotEmpty()) {
                TagList(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 4.dp,
                        bottom = 8.dp,
                    ),
                    tags = tags,
                )
            }
        }
    }
}

@Composable
private fun LiveStream(
    modifier: Modifier = Modifier,
    title: String?,
    userName: String?,
    viewerCount: Int?,
    gameName: String?,
    startedAt: Instant?,
    profileImageURL: String?,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface),
                model = remoteImageModel(profileImageURL),
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
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    viewerCount?.let { viewerCount ->
                        Text(
                            modifier = Modifier.alignByBaseline(),
                            text = viewerCount.formatNumber(),
                            maxLines = 1,
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
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    startedAt
                        ?.formatTimestamp()
                        ?.let { startedAt ->
                            Text(
                                modifier = Modifier.alignByBaseline(),
                                text = startedAt,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                }
            }
        }
    }
}
