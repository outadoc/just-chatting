package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun ContextualButton(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: String,
    contentPadding: PaddingValues = PaddingValues(),
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier =
            Modifier
                .padding(contentPadding)
                .padding(vertical = 12.dp),
        ) {
            icon()

            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
            )
        }
    }
}
