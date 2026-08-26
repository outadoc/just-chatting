package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun PinnedMessageCardScreenshotTest() {
    AppTheme {
        PinnedMessageCard(
            message = PreviewFixtures.sampleChatMessage,
            appUser =
                AppUser.LoggedIn(
                    userId = "",
                    userLogin = "",
                    token = "",
                ),
        )
    }
}
