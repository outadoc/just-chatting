package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentSetOf

@PreviewTest
@Preview
@Composable
internal fun LiveStreamScreenshotTest() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier.padding(8.dp),
            userName = PreviewFixtures.sampleUser.displayName,
            category = PreviewFixtures.sampleStreamCategory,
            title = PreviewFixtures.sampleTextLong,
            viewerCount = 5_305,
            startedAt = PreviewFixtures.sampleTimestamp,
            profileImageUrl = null,
            clock = PreviewFixtures.fixedClock,
            tags =
                persistentSetOf(
                    "French",
                    "Test",
                    "Sponsored",
                    "Label 1",
                    "Super long label with too much text, you can't really argue otherwise",
                ),
        )
    }
}

@PreviewTest
@Preview
@Composable
internal fun LiveStreamLongScreenshotTest() {
    AppTheme {
        LiveStreamCard(
            modifier =
                Modifier
                    .width(250.dp)
                    .padding(8.dp),
            userName = PreviewFixtures.sampleUser.displayName,
            category = PreviewFixtures.sampleStreamCategory,
            title = PreviewFixtures.sampleTextLong,
            viewerCount = 5_305,
            startedAt = PreviewFixtures.sampleTimestamp,
            profileImageUrl = null,
            clock = PreviewFixtures.fixedClock,
        )
    }
}
