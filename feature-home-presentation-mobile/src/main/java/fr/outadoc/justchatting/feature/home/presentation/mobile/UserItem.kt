package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.outadoc.justchatting.feature.chat.presentation.mobile.remoteImageModel
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.formatTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

@ThemePreviews
@Composable
fun UserItemPreview() {
    AppTheme {
        UserItemCard(
            modifier = Modifier
                .padding(8.dp)
                .width(300.dp),
            displayName = "Maghla",
            followedAt = "2022-01-01T13:45:04.00Z".toInstant(),
            profileImageURL = null,
            tags = persistentListOf("French", "ASMR"),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserItemCard(
    modifier: Modifier = Modifier,
    displayName: String? = null,
    followedAt: Instant? = null,
    profileImageURL: String? = null,
    tags: ImmutableList<String> = persistentListOf(),
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        onClick = onClick,
    ) {
        UserItem(
            modifier = Modifier.padding(8.dp),
            displayName = displayName,
            followedAt = followedAt,
            profileImageURL = profileImageURL,
            tags = tags,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserItem(
    modifier: Modifier = Modifier,
    displayName: String?,
    followedAt: Instant?,
    profileImageURL: String?,
    tags: ImmutableList<String>,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface),
            model = remoteImageModel(profileImageURL),
            contentDescription = null,
        )

        Column {
            displayName?.let { displayName ->
                Text(
                    text = displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            followedAt
                ?.formatTime()
                ?.let { followedAt ->
                    Text(
                        text = stringResource(R.string.followed_at, followedAt),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tags.forEach { tag ->
                        StreamTagChip(
                            modifier = Modifier.padding(vertical = 2.dp),
                            tag = tag,
                        )
                    }
                }
            }
        }
    }
}
