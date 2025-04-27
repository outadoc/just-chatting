package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.defaultDragHandleSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun ThreePaneScaffoldScope.DragHandle(
    modifier: Modifier = Modifier,
    state: PaneExpansionState,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val size = LocalMinimumInteractiveComponentSize.current

    Box(
        modifier = modifier.width(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        VerticalDivider()
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceBright,
            modifier =
            modifier
                .height(64.dp)
                .fillMaxWidth()
                .paneExpansionDraggable(
                    state = state,
                    minTouchTargetSize = size,
                    interactionSource = interactionSource,
                    semanticsProperties = state.defaultDragHandleSemantics(),
                ),
        ) { }
    }
}
