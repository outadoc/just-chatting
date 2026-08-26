package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentSetOf
import kotlin.time.Clock
import kotlin.time.Instant

// A fixed clock keeps the rendered "time since" duration stable across the
// update/validate Gradle runs, instead of ticking with the real system clock.
private val fixedClock =
    object : Clock {
        override fun now(): Instant = Instant.parse("2022-01-01T18:00:00Z")
    }

@PreviewTest
@Preview
@Composable
internal fun LiveStreamScreenshotTest() {
    AppTheme {
        LiveStreamCard(
            modifier = Modifier.padding(8.dp),
            userName = "Maghla",
            category =
                StreamCategory(
                    id = "1",
                    name = "Powerwash Simulator",
                ),
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageUrl = null,
            clock = fixedClock,
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
            userName = "Maghla",
            category =
                StreamCategory(
                    id = "1",
                    name = "Powerwash Simulator",
                ),
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            viewerCount = 5_305,
            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
            profileImageUrl = null,
            clock = fixedClock,
        )
    }
}
