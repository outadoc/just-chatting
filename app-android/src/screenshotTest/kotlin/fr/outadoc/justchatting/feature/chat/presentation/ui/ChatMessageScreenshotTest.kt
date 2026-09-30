package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun ChatMessageScreenshotTest() {
    AppTheme {
        ChatMessage(
            timeZone = PreviewFixtures.timeZone,
            message = PreviewFixtures.sampleChatMessage,
            showTimestamps = true,
            appUser = PreviewFixtures.sampleLoggedInUser,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@PreviewTest
@Preview
@Composable
internal fun ChatMessageGigantifiedEmoteScreenshotTest() {
    // Emotes are loaded from the network, so draw a plain square in their place.
    CompositionLocalProvider(
        LocalAsyncImagePreviewHandler provides
            AsyncImagePreviewHandler { ColorImage(color = 0xFFE91E63.toInt(), width = 112, height = 112) },
    ) {
        AppTheme {
            ChatMessage(
                timeZone = PreviewFixtures.timeZone,
                message = PreviewFixtures.sampleGigantifiedEmoteMessage,
                showTimestamps = true,
                appUser = PreviewFixtures.sampleLoggedInUser,
            )
        }
    }
}
