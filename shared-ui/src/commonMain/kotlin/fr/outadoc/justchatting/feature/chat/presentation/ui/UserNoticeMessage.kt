package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.presentation.ui.preview.ChatMessagePreviewProvider
import fr.outadoc.justchatting.feature.chat.presentation.ui.preview.previewBadges
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.toPersistentHashMap

@Preview
@Composable
internal fun UserNoticeMessagePreview(
    @PreviewParameter(ChatMessagePreviewProvider::class) message: ChatListItem.Message,
) {
    val inlineBadges =
        previewBadges
            .associateWith { previewTextContent() }
            .toPersistentHashMap()

    AppTheme {
        ChatMessage(
            message = message,
            inlineContent = inlineBadges,
            showTimestamps = true,
            appUser =
                AppUser.LoggedIn(
                    userId = "123",
                    userLogin = "outadoc",
                    token = ApiToken(""),
                ),
        )
    }
}

@Composable
public fun UserNoticeMessage(
    modifier: Modifier = Modifier,
    title: String,
    titleIcon: ImageVector?,
    subtitle: String?,
    level: ChatListItem.Message.Highlighted.Level,
    iconSize: Dp = 16.dp,
    data: @Composable () -> Unit,
) {
    HighlightedMessageCard(
        modifier = modifier.fillMaxWidth(),
        level = level,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (titleIcon != null) {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        imageVector = titleIcon,
                        contentDescription = null,
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            subtitle?.let {
                Text(
                    modifier =
                        Modifier.padding(
                            start = if (titleIcon != null) iconSize + 6.dp else 0.dp,
                        ),
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            data()
        }
    }
}
