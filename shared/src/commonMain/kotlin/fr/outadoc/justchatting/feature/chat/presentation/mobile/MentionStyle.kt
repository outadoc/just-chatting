package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

internal fun getMentionStyle(
    mentioned: Boolean,
    mentionBackground: Color,
    mentionColor: Color,
): SpanStyle {
    return SpanStyle(
        fontWeight = FontWeight.Bold,
        background = if (mentioned) mentionBackground else Color.Unspecified,
        color = if (mentioned) mentionColor else Color.Unspecified,
    )
}
