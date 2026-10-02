package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.ChatPrefixConstants
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_message_standardSeparator
import fr.outadoc.justchatting.shared.internal.chat_replyingTo
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InReplyToMessage(
    modifier: Modifier = Modifier,
    mentions: ImmutableList<String>,
    message: String?,
    appUser: AppUser.LoggedIn? = null,
    mentionPillColor: Color = MaterialTheme.colorScheme.primaryContainer,
    mentionColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current.copy(alpha = 0.8f),
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier =
                    Modifier
                        .size(16.dp)
                        .alignByBaseline()
                        .padding(end = 4.dp, top = 1.dp),
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = stringResource(Res.string.chat_replyingTo),
            )

            Text(
                modifier =
                    Modifier.drawMentionPills(
                        layoutResult = { layoutResult.value },
                        pillColor = mentionPillColor,
                    ),
                onTextLayout = { layoutResult.value = it },
                text =
                    buildAnnotatedString {
                        mentions.forEachIndexed { index, mention ->
                            if (index > 0) append(' ')

                            appendMention(
                                text = "${ChatPrefixConstants.ChatterPrefix}$mention",
                                mentioned = mention.equals(appUser?.userLogin, ignoreCase = true),
                                mentionColor = mentionColor,
                            )
                        }

                        if (message != null) {
                            append(stringResource(Res.string.chat_message_standardSeparator))
                            append(message)
                        }
                    },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
