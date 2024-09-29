package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.outadoc.justchatting.feature.chat.presentation.mobile.TagList
import fr.outadoc.justchatting.feature.chat.presentation.mobile.remoteImageModel
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.customColors
import fr.outadoc.justchatting.utils.presentation.formatNumber
import fr.outadoc.justchatting.utils.presentation.formatTimeSince
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun LiveStreamCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    userName: String? = null,
    viewerCount: Long? = null,
    category: StreamCategory? = null,
    startedAt: Instant? = null,
    profileImageUrl: String? = null,
    tags: ImmutableSet<String> = persistentSetOf(),
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
                    category = category,
                    startedAt = startedAt,
                    profileImageURL = profileImageUrl,
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
    viewerCount: Long?,
    category: StreamCategory?,
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    userName?.let { userName ->
                        Text(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .alignByBaseline(),
                            text = userName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    viewerCount?.let { viewerCount ->
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.customColors.live),
                        ) {}

                        Text(
                            modifier = Modifier.alignByBaseline(),
                            text = viewerCount.toInt().formatNumber(),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    category?.let { category ->
                        Text(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .alignByBaseline(),
                            text = category.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    startedAt
                        ?.formatTimeSince(showSeconds = false)
                        ?.let { streamDuration ->
                            Icon(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.CenterVertically),
                                imageVector = Icons.Default.Timelapse,
                                contentDescription = null,
                            )

                            Text(
                                modifier = Modifier.alignByBaseline(),
                                text = streamDuration,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun LiveStreamPreview() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier.padding(8.dp),
            userName = "Maghla",
            category = StreamCategory(
                id = "1",
                name = "Powerwash Simulator",
            ),
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageUrl = null,
            tags = persistentSetOf(
                "French",
                "Test",
                "Sponsored",
                "Label 1",
                "Super long label with too much text, you can't really argue otherwise",
            ),
        )
    }
}

@Preview
@Composable
internal fun LiveStreamLongPreview() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier
                .width(250.dp)
                .padding(8.dp),
            userName = "Maghla",
            category = StreamCategory(
                id = "1",
                name = "Powerwash Simulator",
            ),
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageUrl = null,
        )
    }
}
