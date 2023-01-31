package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.preferences.data.AppUser
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun SimpleMessage(
    modifier: Modifier = Modifier,
    message: ChatEvent.Simple,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    animateEmotes: Boolean,
    appUser: AppUser,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
) {
    Row {
        Spacer(modifier = Modifier.width(4.dp))

        ChatMessageData(
            modifier = modifier.padding(
                horizontal = 4.dp,
                vertical = 6.dp,
            ),
            data = message.data,
            inlineContent = inlineContent,
            animateEmotes = animateEmotes,
            appUser = appUser,
            backgroundHint = backgroundHint,
        )
    }
}
