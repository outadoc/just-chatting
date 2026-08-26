package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun PredictionOutcomeScreenshotTest() {
    AppTheme {
        PredictionOutcome(
            title = "Antoine",
            votes = 123,
            totalVotes = 300,
            color = Color.Red,
            icon = {},
        )
    }
}

@PreviewTest
@Preview
@Composable
internal fun PredictionOutcomeWinningScreenshotTest() {
    AppTheme {
        PredictionOutcome(
            modifier = Modifier.width(800.dp),
            title = "Baghera",
            votes = 123,
            totalVotes = 300,
            color = Color.Blue,
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                )
            },
        )
    }
}
