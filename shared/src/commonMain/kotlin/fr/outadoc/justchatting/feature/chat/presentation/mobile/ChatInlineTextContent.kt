package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Icon
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.em
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.utils.presentation.formatNumber

private const val emoteSizeFloat = 1.8
internal val emoteSize = emoteSizeFloat.em

private fun getEmotePlaceholder(ratio: Float = 1f) = Placeholder(
    width = emoteSize * ratio,
    height = emoteSize,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
)

private val badgePlaceholder = Placeholder(
    width = 1.4.em,
    height = 1.4.em,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
)

internal fun emoteTextContent(emote: Emote): InlineTextContent = InlineTextContent(getEmotePlaceholder(ratio = emote.ratio)) {
    EmoteItem(
        emote = emote,
    )
}

internal fun badgeTextContent(badge: TwitchBadge): InlineTextContent = InlineTextContent(badgePlaceholder) {
    BadgeItem(badge = badge)
}

internal fun cheerEmoteTextContent(cheer: Emote): InlineTextContent {
    val textWidthEm: Float = cheer.bitsValue?.let { it.formatNumber().length / 1.8f } ?: 0f
    return InlineTextContent(
        Placeholder(
            width = (emoteSizeFloat + textWidthEm + 0.3f).em,
            height = emoteSize,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
        ),
    ) {
        CheerEmoteItem(
            emote = cheer,
        )
    }
}

internal fun previewTextContent(): InlineTextContent = InlineTextContent(badgePlaceholder) {
    Icon(Icons.Default.Token, contentDescription = null)
}
