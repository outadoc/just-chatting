package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.ChatMessagePreviewProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.previewBadges
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import kotlinx.collections.immutable.toPersistentHashMap

@ThemePreviews
@Composable
internal fun UserNoticeMessagePreview(
    @PreviewParameter(ChatMessagePreviewProvider::class) message: ChatEvent.Message,
) {
    val inlineBadges = previewBadges
        .associateWith { previewTextContent() }
        .toPersistentHashMap()

    AppTheme {
        ChatMessage(
            message = message,
            inlineContent = inlineBadges,
            showTimestamps = true,
            appUser = AppUser.LoggedIn(
                userId = "123",
                userLogin = "outadoc",
                token = "",
            ),
        )
    }
}

@Composable
internal fun UserNoticeMessage(
    modifier: Modifier = Modifier,
    title: String,
    titleIcon: ImageVector?,
    subtitle: String?,
    level: ChatEvent.Message.Highlighted.Level,
    iconSize: Dp = 20.dp,
    data: @Composable () -> Unit,
) {
    HighlightedMessageCard(
        modifier = modifier.fillMaxWidth(),
        level = level,
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (titleIcon != null) {
                    Icon(
                        modifier = Modifier
                            .size(iconSize)
                            .padding(end = 4.dp),
                        imageVector = titleIcon,
                        contentDescription = null,
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            subtitle?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (titleIcon != null) {
                        Spacer(
                            modifier = Modifier
                                .size(iconSize)
                                .padding(end = 4.dp),
                        )
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        data()
    }
}
