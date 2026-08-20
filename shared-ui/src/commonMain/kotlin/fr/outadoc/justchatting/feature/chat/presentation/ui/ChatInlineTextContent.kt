package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.utils.presentation.formatNumber

private const val emoteSizeFloat = 1.8
internal val emoteSize = emoteSizeFloat.em
internal val gigantifiedEmoteSize = (emoteSizeFloat * 4.5).em

private fun getEmotePlaceholder(
    ratio: Float = 1f,
    size: TextUnit = emoteSize,
) = Placeholder(
    width = size * ratio,
    height = size,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
)

private val badgePlaceholder =
    Placeholder(
        width = 1.4.em,
        height = 1.4.em,
        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
    )

internal fun emoteTextContent(
    emote: Emote,
    isGigantified: Boolean = false,
): InlineTextContent =
    InlineTextContent(
        getEmotePlaceholder(
            ratio = emote.ratio,
            size = if (isGigantified) gigantifiedEmoteSize else emoteSize,
        ),
    ) {
        EmoteItem(
            emote = emote,
        )
    }

internal fun badgeTextContent(badge: TwitchBadge): InlineTextContent =
    InlineTextContent(badgePlaceholder) {
        BadgeItem(badge = badge)
    }

internal val TwitchBadge.inlineContentId: String
    get() = "badge_${setId}_$version"

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

internal fun sourceChannelTextContent(user: User): InlineTextContent =
    InlineTextContent(badgePlaceholder) {
        AsyncImage(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
            model = remoteImageModel(user.profileImageUrl),
            contentDescription = user.displayName,
        )
    }

internal fun sourceChannelInlineContentId(roomId: String): String = "source-channel:$roomId"

internal fun previewTextContent(): InlineTextContent =
    InlineTextContent(badgePlaceholder) {
        Icon(Icons.Default.Token, contentDescription = null)
    }
