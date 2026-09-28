package fr.outadoc.justchatting.feature.preferences.presentation.ui

import android.content.res.Configuration
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
        SettingsListSample()
    }
}

@PreviewTest
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun SettingsListDarkScreenshotTest() {
    AppTheme(isDarkTheme = true) {
        SettingsListSample()
    }
}

@Composable
private fun SettingsListSample() {
    SettingsList(
        loggedInUser =
            PreviewFixtures.user(
                id = "1",
                login = "maghla",
                displayName = "Maghla",
                description = PreviewFixtures.sampleTextShort,
                createdAt = PreviewFixtures.sampleTimestamp,
            ),
        appVersionName = "1.0.0",
        onLogoutClick = {},
        onOpenDependencyCredits = {},
        onOpenThirdPartiesSection = {},
        onOpenAppearanceSection = {},
        onOpenAboutSection = {},
        onOpenNotificationSection = {},
    )
}
