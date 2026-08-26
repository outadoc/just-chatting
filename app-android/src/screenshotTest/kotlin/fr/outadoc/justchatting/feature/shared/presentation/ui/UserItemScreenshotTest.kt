package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf

@PreviewTest
@Preview
@Composable
internal fun UserItemScreenshotTest() {
    AppTheme {
        Surface {
            UserItemCard(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .width(300.dp),
                displayName = PreviewFixtures.sampleUser.displayName,
                followedAt = PreviewFixtures.sampleTimestamp,
                profileImageUrl = null,
                tags = persistentListOf("French", "ASMR"),
            )
        }
    }
}
