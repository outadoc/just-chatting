package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import fr.outadoc.justchatting.feature.chat.presentation.ChatPrefixConstants

internal fun getMentionStyle(
    mentioned: Boolean,
    mentionBackground: Color,
    mentionColor: Color,
): SpanStyle =
    SpanStyle(
        fontWeight = FontWeight.Bold,
        background = if (mentioned) mentionBackground else Color.Unspecified,
        color = if (mentioned) mentionColor else Color.Unspecified,
    )

/**
 * Whether [mention], a word from a chat message such as `@login` or `@login,`, mentions the user
 * with the given [login]. The mention prefix and any trailing punctuation are ignored.
 */
internal fun isMentionOf(
    mention: String,
    login: String,
): Boolean {
    val mentionedName =
        mention
            .removePrefix(ChatPrefixConstants.ChatterPrefix.toString())
            .trimEnd { char -> !char.isLetterOrDigit() && char != '_' }

    return mentionedName.isNotEmpty() && mentionedName.equals(login, ignoreCase = true)
}
