package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.ChatEntryPreviewProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.previewBadges
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import kotlinx.collections.immutable.toPersistentHashMap

@ThemePreviews
@Composable
fun HighlightedMessagePreview(
    @PreviewParameter(ChatEntryPreviewProvider::class) message: ChatEvent.Message,
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
                id = "123",
                login = "outadoc",
                helixToken = "",
            ),
        )
    }
}

@Composable
fun HighlightedMessage(
    modifier: Modifier = Modifier,
    title: String,
    titleIcon: ImageVector?,
    subtitle: String?,
    iconSize: Dp = 20.dp,
    data: @Composable () -> Unit,
) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colorScheme.primary)
                .width(4.dp)
                .fillMaxHeight(),
        )

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            shape = RectangleShape,
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
}
