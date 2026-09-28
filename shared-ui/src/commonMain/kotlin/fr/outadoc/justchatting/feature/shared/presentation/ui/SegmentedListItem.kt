package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object SegmentedListDefaults {
    /**
     * Vertical gap between two consecutive items of the same group.
     */
    val ItemSpacing: Dp = 2.dp

    val OuterCornerRadius: Dp = 20.dp
    val InnerCornerRadius: Dp = 4.dp

    val StandaloneShape: Shape = RoundedCornerShape(OuterCornerRadius)

    /**
     * Returns the shape of the item at [index] in a group of [count] items: the first item
     * has large top corners, the last one has large bottom corners, and everything in between
     * uses small corners so the group reads as a single segmented block.
     */
    @Stable
    fun shape(
        index: Int,
        count: Int,
    ): Shape {
        val isFirst = index == 0
        val isLast = index == count - 1
        return RoundedCornerShape(
            topStart = if (isFirst) OuterCornerRadius else InnerCornerRadius,
            topEnd = if (isFirst) OuterCornerRadius else InnerCornerRadius,
            bottomStart = if (isLast) OuterCornerRadius else InnerCornerRadius,
            bottomEnd = if (isLast) OuterCornerRadius else InnerCornerRadius,
        )
    }
}

/**
 * A container for one item of a segmented list. The content is clipped to [shape], so any
 * clickable modifier applied inside [content] gets a correctly shaped ripple.
 */
@Composable
internal fun SegmentedListItem(
    modifier: Modifier = Modifier,
    shape: Shape = SegmentedListDefaults.StandaloneShape,
    isSelected: Boolean = false,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color =
            if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
    ) {
        Box {
            content()
        }
    }
}
