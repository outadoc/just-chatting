package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun NoticeMessageScreenshotTest() {
    AppTheme {
        NoticeMessage(
            text = "This room is now in followers-only mode.",
        )
    }
}
