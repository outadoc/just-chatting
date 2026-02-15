package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.ChatMessagePreviewProvider
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.poll_collapse_action
import fr.outadoc.justchatting.shared.poll_expand_action
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun PinnedMessageCard(
    modifier: Modifier = Modifier,
    message: ChatListItem.Message,
    appUser: AppUser.LoggedIn,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    inlineContent: ImmutableMap<String, InlineTextContent> = persistentMapOf(),
    removedContent: ImmutableList<ChatListItem.RemoveContent> = persistentListOf(),
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }
    val maxLines by animateIntAsState(
        targetValue = if (isExpanded) 24 else 2,
        label = "message max lines",
    )

    Card(
        modifier = modifier,
        onClick = { isExpanded = !isExpanded },
        colors =
        CardDefaults.cardColors(
            containerColor = color,
        ),
    ) {
        Row(
            modifier =
            Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChatMessage(
                modifier =
                Modifier
                    .weight(1f, fill = true),
                message = message,
                inlineContent = inlineContent,
                removedContent = removedContent,
                showTimestamps = false,
                background = color,
                appUser = appUser,
                maxLines = maxLines,
            )

            if (isExpanded) {
                Icon(
                    Icons.Default.ArrowDropUp,
                    contentDescription = stringResource(Res.string.poll_collapse_action),
                )
            } else {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(Res.string.poll_expand_action),
                )
            }
        }
    }
}

@Preview
@Composable
internal fun PinnedMessageCardPreview(
    @PreviewParameter(ChatMessagePreviewProvider::class) message: ChatListItem.Message,
) {
    AppTheme {
        PinnedMessageCard(
            message = message,
            appUser =
            AppUser.LoggedIn(
                userId = "",
                userLogin = "",
                token = "",
            ),
        )
    }
}
