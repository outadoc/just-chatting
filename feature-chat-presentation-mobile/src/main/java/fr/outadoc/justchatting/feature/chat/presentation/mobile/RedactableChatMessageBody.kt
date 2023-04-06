package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Pronoun
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.SimpleChatMessagePreviewProvider
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

@Composable
fun RedactableChatMessageBody(
    modifier: Modifier = Modifier,
    timestamp: Instant,
    body: ChatEvent.Message.Body,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    knownChatters: PersistentSet<Chatter>,
    pronouns: ImmutableMap<Chatter, Pronoun>,
    removedContent: ImmutableList<ChatEvent.RemoveContent> = persistentListOf(),
    appUser: AppUser.LoggedIn,
    backgroundHint: Color,
    richEmbed: ChatEvent.RichEmbed? = null,
) {
    val shouldRedactContents: Boolean =
        removedContent
            .filter { rule -> rule.upUntil > timestamp }
            .filter { rule -> rule.matchingMessageId == null || rule.matchingMessageId == body.messageId }
            .any { rule -> rule.matchingUserId == null || rule.matchingUserId == body.chatter.id }

    var overrideRedaction: Boolean by remember { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        if (overrideRedaction) 0.dp else 6.dp,
        label = "redaction radius",
    )

    ChatMessageBody(
        modifier = modifier
            .then(
                if (shouldRedactContents) {
                    Modifier
                        .blur(
                            radius = blurRadius,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    try {
                                        overrideRedaction = true
                                        awaitRelease()
                                    } finally {
                                        overrideRedaction = false
                                    }
                                },
                            )
                        }
                } else {
                    Modifier
                },
            ),
        body = body,
        inlineContent = inlineContent,
        knownChatters = knownChatters,
        pronouns = pronouns,
        appUser = appUser,
        backgroundHint = backgroundHint,
        richEmbed = richEmbed,
    )
}

@ThemePreviews
@Composable
fun RedactableChatMessageBodyPreview(
    @PreviewParameter(SimpleChatMessagePreviewProvider::class) message: ChatEvent.Message.Simple,
) {
    AppTheme {
        Surface {
            RedactableChatMessageBody(
                timestamp = message.timestamp,
                body = message.body,
                inlineContent = persistentMapOf(),
                knownChatters = persistentSetOf(),
                pronouns = persistentMapOf(),
                removedContent = persistentListOf(
                    ChatEvent.RemoveContent(message.timestamp + 1.minutes),
                ),
                appUser = AppUser.LoggedIn(
                    id = "",
                    login = "",
                    helixToken = "",
                ),
                backgroundHint = Color.White,
            )
        }
    }
}
