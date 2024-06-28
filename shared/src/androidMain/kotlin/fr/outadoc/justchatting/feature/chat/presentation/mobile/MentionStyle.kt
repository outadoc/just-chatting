package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

internal fun getMentionStyle(
    mentioned: Boolean,
    mentionBackground: Color,
    mentionColor: Color,
): SpanStyle {
    return if (mentioned) {
        SpanStyle(
            background = mentionBackground,
            color = mentionColor,
        )
    } else {
        SpanStyle()
    }
}
