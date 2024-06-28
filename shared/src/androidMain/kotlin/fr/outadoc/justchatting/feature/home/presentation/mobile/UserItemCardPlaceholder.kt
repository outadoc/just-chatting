package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer

@Composable
internal fun UserItemCardPlaceholder(modifier: Modifier = Modifier) {
    UserItemCard(
        modifier = modifier.placeholder(
            visible = true,
            shape = CardDefaults.shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            highlight = PlaceholderHighlight.shimmer(),
        ),
    )
}
