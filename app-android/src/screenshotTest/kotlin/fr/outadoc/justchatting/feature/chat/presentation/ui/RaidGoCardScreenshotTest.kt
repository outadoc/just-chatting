package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun RaidGoCardScreenshotTest() {
    AppTheme {
        RaidGoCard(
            raid =
                Raid.Go(
                    targetId = "",
                    targetLogin = "",
                    targetDisplayName = "HortyUnderscore",
                    targetProfileImageUrl = null,
                    viewerCount = 12_000,
                ),
        )
    }
}
