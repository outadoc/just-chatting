package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.presentation.ui.StreamTagChip
import kotlinx.collections.immutable.ImmutableSet

@Composable
internal fun TagList(
    modifier: Modifier = Modifier,
    tags: ImmutableSet<String>,
) {
    FlowRow(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            StreamTagChip(
                modifier = Modifier.padding(vertical = 2.dp),
                tag = tag,
            )
        }
    }
}
