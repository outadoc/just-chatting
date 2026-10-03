package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.SharedChatChannel
import fr.outadoc.justchatting.feature.shared.presentation.ui.DetailsListItem
import fr.outadoc.justchatting.feature.shared.presentation.ui.DetailsListItemDefaults
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatar
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.sharedChat_host
import fr.outadoc.justchatting.shared.internal.sharedChat_title
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/**
 * Lists the other channels taking part in the current shared chat session.
 */
@Composable
public fun SharedChatCard(
    modifier: Modifier = Modifier,
    channels: ImmutableList<SharedChatChannel>,
    onChannelClick: ((SharedChatChannel) -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.sharedChat_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Column {
                channels.forEach { channel ->
                    SharedChatChannelItem(
                        channel = channel,
                        onClick = onChannelClick?.let { { onChannelClick(channel) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun SharedChatChannelItem(
    modifier: Modifier = Modifier,
    channel: SharedChatChannel,
    onClick: (() -> Unit)? = null,
) {
    DetailsListItem(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            UserAvatar(
                profileImageUrl = channel.user.profileImageUrl,
                size = DetailsListItemDefaults.LeadingContentHeight,
            )
        },
        headlineContent = {
            Text(
                text = channel.user.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent =
            if (channel.isHost) {
                {
                    Text(
                        text = stringResource(Res.string.sharedChat_host),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                null
            },
    )
}
