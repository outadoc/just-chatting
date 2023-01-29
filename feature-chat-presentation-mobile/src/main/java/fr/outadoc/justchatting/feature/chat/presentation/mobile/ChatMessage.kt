package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.ChatEntry
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.ChatEntryPreviewProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.previewBadges
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.formatTimestamp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap

@ThemePreviews
@Composable
fun ChatMessagePreview(
    @PreviewParameter(ChatEntryPreviewProvider::class) chatEntry: ChatEntry,
) {
    val inlineBadges = previewBadges
        .associateWith { previewTextContent() }
        .toPersistentHashMap()

    AppTheme {
        ChatMessage(
            message = chatEntry,
            inlineContent = inlineBadges,
            animateEmotes = true,
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
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    background: Color = Color.Transparent,
    appUser: AppUser,
) {
    val timestamp = message.timestamp
        .formatTimestamp()
        ?.takeIf { showTimestamps }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .background(background)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (timestamp != null) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = timestamp,
                color = LocalContentColor.current.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        when (message) {
            is ChatEntry.Highlighted -> {
                HighlightedMessage(
                    message = message,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes,
                    appUser = appUser,
                )
            }

            is ChatEntry.Simple -> {
                SimpleMessage(
                    message = message,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes,
                    appUser = appUser,
                )
            }
        }
    }
}
