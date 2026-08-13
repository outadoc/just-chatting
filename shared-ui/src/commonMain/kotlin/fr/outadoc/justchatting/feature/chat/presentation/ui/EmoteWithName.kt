package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.shared.presentation.LabelChip

@Composable
internal fun EmoteWithName(
    modifier: Modifier = Modifier,
    emote: Emote,
) {
    LabelChip(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            EmoteItem(
                modifier = Modifier.size(24.dp),
                emote = emote,
            )

            Text(
                text = emote.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
