package fr.outadoc.justchatting.feature.onboarding.presentation.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun ConnectionErrorScreenScreenshotTest() {
    AppTheme {
        ConnectionErrorScreen(
            onRetryClick = {},
            onLogoutClick = {},
        )
    }
}

@PreviewTest
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ConnectionErrorScreenDarkScreenshotTest() {
    AppTheme(isDarkTheme = true) {
        ConnectionErrorScreen(
            onRetryClick = {},
            onLogoutClick = {},
        )
    }
}
