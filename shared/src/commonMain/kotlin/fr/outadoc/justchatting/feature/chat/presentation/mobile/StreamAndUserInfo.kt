package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun StreamAndUserInfo(
    modifier: Modifier = Modifier,
    user: User?,
    stream: Stream?,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (user != null) {
            UserInfo(user = user)
        }

        if (stream != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                StreamInfo(
                    modifier = Modifier.padding(16.dp),
                    stream = stream,
                )
            }
        }

        if (user == null && stream == null) {
            Text(
                text = stringResource(MR.strings.info_loadError),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview
@Composable
internal fun StreamInfoPreviewFull() {
    AppTheme {
        StreamAndUserInfo(
            user = User(
                id = "",
                login = "",
                displayName = "outadoc",
                description = "Lorem ipsum dolor sit amet",
                profileImageUrl = "",
                createdAt = Instant.DISTANT_PAST,
                usedAt = Instant.DISTANT_PAST,
            ),
            stream = Stream(
                id = "",
                userId = "",
                category = StreamCategory(
                    id = "1",
                    name = "Powerwash Simulator",
                ),
                title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
                viewerCount = 10_000,
                startedAt = Instant.parse("2022-09-01T00:00:00.00Z"),
                tags = persistentSetOf("French"),
            ),
        )
    }
}

@Preview
@Composable
internal fun StreamInfoPreviewOffline() {
    AppTheme {
        StreamAndUserInfo(
            user = User(
                id = "",
                login = "",
                displayName = "outadoc",
                description = "Lorem ipsum dolor sit amet",
                profileImageUrl = "",
                createdAt = Instant.DISTANT_PAST,
                usedAt = Instant.DISTANT_PAST,
            ),
            stream = null,
        )
    }
}

@Preview
@Composable
internal fun StreamInfoPreviewError() {
    AppTheme {
        StreamAndUserInfo(
            user = null,
            stream = null,
        )
    }
}