package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun SettingsListScreenshotTest() {
    AppTheme {
        SettingsList(
            loggedInUser =
                User(
                    id = "123",
                    login = "maghla",
                    displayName = "Maghla",
                    description = "",
                    profileImageUrl = "",
                    createdAt = Instant.DISTANT_PAST,
                    usedAt = Instant.DISTANT_PAST,
                ),
            onLogoutClick = {},
            onOpenDependencyCredits = {},
            onOpenThirdPartiesSection = {},
            onOpenAppearanceSection = {},
            onOpenAboutSection = {},
            onOpenNotificationSection = {},
        )
    }
}
