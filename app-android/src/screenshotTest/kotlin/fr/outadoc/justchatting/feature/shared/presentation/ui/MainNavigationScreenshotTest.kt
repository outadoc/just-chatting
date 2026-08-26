package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun MainNavigationScreenshotTest() {
    AppTheme {
        MainNavigation(
            selectedScreen = Screen.Live,
            onSelectedTabChange = {},
        ) {
            Text("Content")
        }
    }
}
