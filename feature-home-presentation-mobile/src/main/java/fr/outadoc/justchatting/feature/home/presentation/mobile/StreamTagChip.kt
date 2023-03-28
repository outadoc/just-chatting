package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun StreamTagChip(
    tag: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current.copy(alpha = 0.8f),
) {
    SuggestionChip(
        modifier = modifier
            .padding(vertical = 2.dp)
            .height(24.dp),
        enabled = false,
        shape = MaterialTheme.shapes.extraLarge,
        colors = SuggestionChipDefaults.suggestionChipColors(
            disabledLabelColor = color,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            disabledBorderColor = color,
        ),
        onClick = {},
        label = {
            Text(tag, overflow = TextOverflow.Ellipsis)
        },
    )
}
