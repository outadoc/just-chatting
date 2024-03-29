package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews

@Composable
fun StreamAndUserInfo(
    modifier: Modifier = Modifier,
    user: User,
    stream: Stream?,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        UserInfo(user = user)

        if (stream != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                StreamInfo(
                    modifier = Modifier.padding(16.dp),
                    stream = stream,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun StreamInfoPreviewFull() {
    AppTheme {
        StreamAndUserInfo(
            user = User(
                id = "",
                login = "",
                description = "Lorem ipsum dolor sit amet",
                displayName = "outadoc",
                createdAt = "2022-01-01T00:00:00.00Z",
            ),
            stream = Stream(
                id = "",
                userId = "",
                userLogin = "",
                userName = "",
                profileImageURL = null,
                gameName = "Powerwash Simulator",
                title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
                viewerCount = 10_000,
                startedAt = "2022-09-01T00:00:00.00Z",
                tags = listOf("French"),
            ),
        )
    }
}

@ThemePreviews
@Composable
fun StreamInfoPreviewOffline() {
    AppTheme {
        StreamAndUserInfo(
            user = User(
                id = "",
                login = "",
                displayName = "outadoc",
                description = "Lorem ipsum dolor sit amet",
                createdAt = "2022-01-01T00:00:00.00Z",
            ),
            stream = null,
        )
    }
}
