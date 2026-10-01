package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.SharedChatChannel
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatar
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatarDefaults
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.sharedChat_host
import fr.outadoc.justchatting.shared.internal.sharedChat_title
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/**
 * Lists the other channels taking part in the current shared chat session.
 */
@Composable
internal fun SharedChatCard(
    modifier: Modifier = Modifier,
    channels: ImmutableList<SharedChatChannel>,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.sharedChat_title),
                style = MaterialTheme.typography.titleMedium,
            )

            channels.forEach { channel ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UserAvatar(
                        profileImageUrl = channel.user.profileImageUrl,
                        size = 24.dp,
                        shape = UserAvatarDefaults.SmallShape,
                    )

                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = channel.user.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (channel.isHost) {
                        Text(
                            text = stringResource(Res.string.sharedChat_host),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
