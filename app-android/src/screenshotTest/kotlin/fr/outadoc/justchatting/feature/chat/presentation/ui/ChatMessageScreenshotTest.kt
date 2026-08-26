package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun ChatMessageScreenshotTest() {
    AppTheme {
        ChatMessage(
            message =
                ChatListItem.Message.Simple(
                    body =
                        ChatListItem.Message.Body(
                            chatter =
                                Chatter(
                                    displayName = "Hiccoz",
                                    id = "68552712",
                                    login = "hiccoz",
                                ),
                            message = "feur",
                            messageId = "b43bd9e5-ec6e-47fe-a5da-c3213540fe06",
                            isAction = false,
                            color = "#FF69B4",
                            badges =
                                persistentListOf(
                                    Badge("subscriber", "48"),
                                    Badge("sub-gifter", "100"),
                                ),
                        ),
                    timestamp = Instant.fromEpochMilliseconds(1664396374382),
                ),
            showTimestamps = true,
            appUser =
                AppUser.LoggedIn(
                    userId = "123",
                    userLogin = "outadoc",
                    token = "",
                ),
        )
    }
}
