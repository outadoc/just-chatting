package fr.outadoc.justchatting.feature.shared.presentation.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.padding

@Composable
internal fun GlanceCard(
    modifier: GlanceModifier = GlanceModifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = modifier
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp,
                )
                .appCornerRadius()
                .background(GlanceTheme.colors.surfaceVariant),
        ) {
            content()
        }
    }
}
