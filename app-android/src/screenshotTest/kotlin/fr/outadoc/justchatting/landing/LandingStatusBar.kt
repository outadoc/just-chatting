package fr.outadoc.justchatting.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Draws [content] below a status bar, as on a device. Screenshot tests don't draw the system
 * bars, and without one, the top app bar looks cut off by the rounded corners of the device
 * frames the landing page shows the full-size screenshots in.
 *
 * @param color the color of what's at the top of [content]: apps draw behind the status bar.
 */
@Composable
internal fun WithStatusBar(
    color: Color,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LandingStatusBar(color = color)
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun LandingStatusBar(color: Color) {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(color)
                .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            // A fixed time, so that the screenshots don't depend on the time zone.
            text = "12:05",
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusIcon(Icons.Filled.Wifi, contentColor)
            StatusIcon(Icons.Filled.SignalCellular4Bar, contentColor)
            StatusIcon(Icons.Filled.BatteryFull, contentColor)
        }
    }
}

@Composable
private fun StatusIcon(
    icon: ImageVector,
    color: Color,
) {
    Icon(
        modifier = Modifier.size(16.dp),
        imageVector = icon,
        tint = color,
        contentDescription = null,
    )
}
