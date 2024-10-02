package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.utils.presentation.customColors

@Composable
internal fun LiveIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.customColors.live),
    ) {}
}