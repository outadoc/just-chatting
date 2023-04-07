package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.PinnedMessage
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinnedMessageCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    pinnedMessage: PinnedMessage,
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        onClick = { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = color,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = pinnedMessage.message.sender.displayName,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text = pinnedMessage.message.content.text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (isExpanded) {
                    Icon(
                        Icons.Default.ArrowDropUp,
                        contentDescription = stringResource(R.string.poll_collapse_action),
                    )
                } else {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.poll_expand_action),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = "Pinned by ${pinnedMessage.pinnedBy.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private val mockMessage = PinnedMessage(
    pinId = "",
    pinnedBy = PinnedMessage.User(
        userId = "",
        displayName = "outadoc",
    ),
    message = PinnedMessage.Message(
        messageId = "",
        sender = PinnedMessage.User(
            userId = "",
            displayName = "AntoineDaniel",
        ),
        content = PinnedMessage.Message.Content(
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer scelerisque lobortis neque, eu malesuada libero. Proin bibendum purus eu nunc tristique, et condimentum urna elementum.",
        ),
        startsAt = Instant.parse("2023-02-05T18:11:52.832Z"),
        endsAt = Instant.parse("2023-02-05T18:11:52.832Z"),
    ),
)

@ThemePreviews
@Composable
fun PinnedMessageCardPreview() {
    AppTheme {
        PinnedMessageCard(
            pinnedMessage = mockMessage,
        )
    }
}
