package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListDefaults
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListItem
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatar
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatarDefaults
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.all_showDetails_cd
import fr.outadoc.justchatting.shared.internal.chat_open_action
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatNumber
import fr.outadoc.justchatting.utils.presentation.formatTimeSince
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
public fun LiveStreamCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    userName: String? = null,
    viewerCount: Long? = null,
    category: StreamCategory? = null,
    startedAt: Instant? = null,
    profileImageUrl: String? = null,
    tags: ImmutableList<String> = persistentListOf(),
    isSelected: Boolean = false,
    shape: Shape = SegmentedListDefaults.StandaloneShape,
    clock: Clock = Clock.System,
    onUserClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    SegmentedListItem(
        modifier = modifier,
        shape = shape,
        isSelected = isSelected,
    ) {
        LiveStream(
            modifier =
                Modifier
                    .combinedClickable(
                        onClick = onClick,
                        onClickLabel = stringResource(Res.string.chat_open_action),
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        },
                        onLongClickLabel = stringResource(Res.string.all_showDetails_cd),
                    ).padding(horizontal = 16.dp, vertical = 12.dp),
            title = title,
            userName = userName,
            viewerCount = viewerCount,
            category = category,
            startedAt = startedAt,
            profileImageUrl = profileImageUrl,
            tags = tags,
            clock = clock,
            onUserClick = onUserClick,
        )
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
    profileImageUrl: String?,
    tags: ImmutableList<String>,
    clock: Clock = Clock.System,
    onUserClick: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UserAvatar(
            modifier = Modifier.clickable(onClick = onUserClick),
            profileImageUrl = profileImageUrl,
            size = UserAvatarDefaults.LargeSize,
            shape = UserAvatarDefaults.LargeShape,
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = userName.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )

                viewerCount?.let { viewerCount ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LiveIndicator()

                        Text(
                            text = viewerCount.toInt().formatNumber(),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            title?.let { title ->
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            val streamDuration: String? =
                startedAt?.formatTimeSince(clock = clock, showSeconds = false)

            val subtitle: String =
                listOfNotNull(category?.name, streamDuration)
                    .filter { it.isNotEmpty() }
                    .joinToString(separator = " · ")

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (tags.isNotEmpty()) {
                SingleLineTagList(
                    modifier = Modifier.padding(top = 8.dp),
                    tags = tags,
                )
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
            category =
                StreamCategory(
                    id = "1",
                    name = "Powerwash Simulator",
                ),
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageUrl = null,
            tags = persistentListOf("French", "Test", "Sponsored", "Label 1", "Label 2", "Label 3"),
        )
    }
}

@Preview
@Composable
internal fun LiveStreamLongPreview() {
    AppTheme {
        LiveStreamCard(
            modifier =
                Modifier
                    .width(250.dp)
                    .padding(8.dp),
            userName = "Maghla",
            category =
                StreamCategory(
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
