package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.followed_at
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatDate
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
public fun UserItemCard(
    modifier: Modifier = Modifier,
    displayName: String? = null,
    followedAt: Instant? = null,
    profileImageUrl: String? = null,
    shape: Shape = SegmentedListDefaults.StandaloneShape,
    trailingActions: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    SegmentedListItem(
        modifier = modifier,
        shape = shape,
    ) {
        UserItem(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .heightIn(min = if (followedAt != null) 72.dp else 64.dp)
                    .padding(
                        start = 16.dp,
                        end = if (trailingActions != null) 4.dp else 16.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    ),
            displayName = displayName,
            followedAt = followedAt,
            profileImageUrl = profileImageUrl,
            trailingActions = trailingActions,
        )
    }
}

@Composable
internal fun UserItem(
    modifier: Modifier = Modifier,
    displayName: String?,
    followedAt: Instant?,
    profileImageUrl: String?,
    trailingActions: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UserAvatar(
            profileImageUrl = profileImageUrl,
            size = UserAvatarDefaults.MediumSize,
            shape = UserAvatarDefaults.SmallShape,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )

            followedAt
                ?.formatDate()
                ?.let { followedAt ->
                    Text(
                        text = stringResource(Res.string.followed_at, followedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
        }

        trailingActions?.invoke()
    }
}

@Preview
@Composable
internal fun UserItemPreview() {
    AppTheme {
        UserItemCard(
            modifier =
                Modifier
                    .padding(8.dp)
                    .width(300.dp),
            displayName = "Maghla",
            followedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageUrl = null,
        )
    }
}
