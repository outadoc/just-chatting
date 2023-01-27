package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.domain.model.CheerEmote
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.parseHexColor

@Composable
fun CheerEmoteItem(
    modifier: Modifier = Modifier,
    emote: CheerEmote,
    animateEmotes: Boolean,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmoteItem(
            modifier = Modifier.aspectRatio(1f),
            emote = emote,
            animateEmotes = animateEmotes,
        )

        Text(
            modifier = Modifier.padding(
                bottom = 1.dp,
            ),
            text = emote.minBits.formatNumber(),
            color = emote.color?.parseHexColor() ?: LocalContentColor.current,
            fontWeight = FontWeight.Bold,
        )
    }
}