package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun SettingsListScreenshotTest() {
    AppTheme {
        SettingsList(
            loggedInUser = PreviewFixtures.sampleUser,
            onLogoutClick = {},
            onOpenDependencyCredits = {},
            onOpenThirdPartiesSection = {},
            onOpenAppearanceSection = {},
            onOpenAboutSection = {},
            onOpenNotificationSection = {},
        )
    }
}
