package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun UserNoticeMessageScreenshotTest() {
    AppTheme {
        UserNoticeMessage(
            title = "clo_chette_",
            titleIcon = Icons.Default.Star,
            subtitle = "subscribed at Tier 1. They've subscribed for 18 months!",
            level = ChatListItem.Message.Highlighted.Level.Base,
        ) {
            Text("Top 1 on y croit")
        }
    }
}
