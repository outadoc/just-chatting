package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Icon
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.em
import fr.outadoc.justchatting.component.chatapi.domain.model.CheerEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.chat.data.model.TwitchChatEmote
import fr.outadoc.justchatting.utils.core.formatNumber

private val emoteSizeFloat = 1.8
internal val emoteSize = emoteSizeFloat.em

private val emotePlaceholder = Placeholder(
    width = emoteSize,
    height = emoteSize,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
)

private val badgePlaceholder = Placeholder(
    width = 1.4.em,
    height = 1.4.em,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
)

fun emoteTextContent(emote: Emote, animateEmotes: Boolean): InlineTextContent =
    InlineTextContent(emotePlaceholder) {
        EmoteItem(
            emote = emote,
            animateEmotes = animateEmotes,
        )
    }

fun emoteTextContent(emote: TwitchChatEmote, animateEmotes: Boolean): InlineTextContent =
    InlineTextContent(emotePlaceholder) {
        ChatEmoteItem(
            emote = emote,
            animateEmotes = animateEmotes,
        )
    }

fun badgeTextContent(badge: TwitchBadge): InlineTextContent =
    InlineTextContent(badgePlaceholder) {
        BadgeItem(badge = badge)
    }

fun cheerEmoteTextContent(cheer: CheerEmote, animateEmotes: Boolean): InlineTextContent {
    val textWidthEm: Float = cheer.minBits.formatNumber().length / 1.8f
    return InlineTextContent(
        Placeholder(
            width = (emoteSizeFloat + textWidthEm + 0.3f).em,
            height = emoteSize,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
        ),
    ) {
        CheerEmoteItem(
            emote = cheer,
            animateEmotes = animateEmotes,
        )
    }
}

fun previewTextContent(): InlineTextContent =
    InlineTextContent(badgePlaceholder) {
        Icon(Icons.Default.Token, contentDescription = null)
    }
