package fr.outadoc.justchatting.utils.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun LabelChip(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = LocalContentColor.current.copy(alpha = 0.8f),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = color,
                shape = MaterialTheme.shapes.small,
            )
            .padding(
                vertical = 4.dp,
                horizontal = 8.dp,
            ),
    ) {
        ProvideTextStyle(style) {
            content()
        }
    }
}

@ThemePreviews
@Composable
fun LabelChipPreview() {
    AppTheme {
        Surface {
            LabelChip {
                Text("Lorem ipsum.")
            }
        }
    }
}
