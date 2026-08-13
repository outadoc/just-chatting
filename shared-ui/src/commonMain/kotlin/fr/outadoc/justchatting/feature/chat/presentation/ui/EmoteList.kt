package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun EmoteList(
    modifier: Modifier = Modifier,
    emotes: ImmutableList<Emote>,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        emotes
            .distinctBy { emote -> emote.name }
            .forEach { emote ->
                EmoteWithName(
                    modifier = Modifier.padding(vertical = 2.dp),
                    emote = emote,
                )
            }
    }
}
