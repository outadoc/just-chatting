package fr.outadoc.justchatting.feature.shared.presentation.glance

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.LocalSize
import androidx.glance.appwidget.lazy.GridCells
import kotlin.math.floor

@Composable
internal fun adaptiveGridCellsCompat(
    minSize: Dp,
): GridCells {
    return if (Build.VERSION.SDK_INT >= 31) {
        GridCells.Adaptive(minSize = minSize)
    } else {
        val widgetWidth = LocalSize.current.width
        val supportedRange = 1..5
        GridCells.Fixed(
            count =
                floor(widgetWidth / minSize)
                    .toInt()
                    .coerceIn(supportedRange),
        )
    }
}
