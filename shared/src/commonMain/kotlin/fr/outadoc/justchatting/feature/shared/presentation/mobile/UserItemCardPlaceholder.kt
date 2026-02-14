package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.core.PlaceholderHighlight
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.material3.placeholder
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.material3.shimmer

@Composable
internal fun UserItemCardPlaceholder(modifier: Modifier = Modifier) {
    UserItemCard(
        modifier =
        modifier.placeholder(
            visible = true,
            shape = CardDefaults.shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            highlight = PlaceholderHighlight.shimmer(),
        ),
    )
}
