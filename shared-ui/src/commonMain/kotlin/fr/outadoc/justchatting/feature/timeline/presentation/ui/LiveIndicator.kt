package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.utils.presentation.customColors

@Composable
internal fun LiveIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.customColors.live,
                    shape = CircleShape,
                ),
    )
}
