package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.presentation.parseHexColor

@Composable
internal fun CheerEmoteItem(
    modifier: Modifier = Modifier,
    emote: Emote,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmoteItem(
            modifier = Modifier.aspectRatio(1f),
            emote = emote,
        )

        Text(
            text = emote.bitsValue?.formatNumber() ?: "",
            color = emote.colorHex?.parseHexColor() ?: LocalContentColor.current,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
