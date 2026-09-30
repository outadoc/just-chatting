package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun ChatInputBasicScreenshotTest() {
    AppTheme {
        ChatInput(
            message = TextFieldValue("Lorem ipsum KEKW"),
        )
    }
}

@PreviewTest
@Preview
@Composable
internal fun ChatInputLongMessageScreenshotTest() {
    AppTheme {
        ChatInput(
            message = TextFieldValue(PreviewFixtures.sampleTextLong),
        )
    }
}

@PreviewTest
@Preview
@Composable
internal fun ChatInputEmptyScreenshotTest() {
    AppTheme {
        ChatInput()
    }
}

// The placeholder must stay on a single line, even when there is little room for it.
@PreviewTest
@Preview(widthDp = 220, locale = "fr")
@Composable
internal fun ChatInputEmptyNarrowScreenshotTest() {
    AppTheme {
        ChatInput()
    }
}

@PreviewTest
@Preview
@Composable
internal fun ChatInputReplyingScreenshotTest() {
    AppTheme {
        ChatInput(
            replyingTo =
                ChatListItem.Message.Simple(
                    body =
                        ChatListItem.Message.Body(
                            message = "Lorem ipsum dolor sit amet?",
                            messageId = "",
                            chatter =
                                Chatter(
                                    id = "",
                                    displayName = "AntoineDaniel",
                                    login = "",
                                ),
                        ),
                    timestamp = Instant.parse("2022-01-01T00:00:00.00Z"),
                ),
        )
    }
}
