package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.presentation.ui.StreamTagChip
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun TagList(
    modifier: Modifier = Modifier,
    tags: ImmutableList<String>,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            StreamTagChip(tag = tag)
        }
    }
}
