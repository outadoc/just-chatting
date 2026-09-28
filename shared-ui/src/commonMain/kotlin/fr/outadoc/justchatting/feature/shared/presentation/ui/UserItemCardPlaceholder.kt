package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import fr.outadoc.justchatting.feature.shared.presentation.ui.placeholder.core.PlaceholderHighlight
import fr.outadoc.justchatting.feature.shared.presentation.ui.placeholder.material3.placeholder
import fr.outadoc.justchatting.feature.shared.presentation.ui.placeholder.material3.shimmer

@Composable
internal fun UserItemCardPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = SegmentedListDefaults.StandaloneShape,
) {
    UserItemCard(
        modifier =
            modifier.placeholder(
                visible = true,
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                highlight = PlaceholderHighlight.shimmer(),
            ),
        shape = shape,
    )
}
