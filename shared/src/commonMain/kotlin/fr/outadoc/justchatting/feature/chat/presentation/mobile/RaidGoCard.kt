package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.chat.presentation.ChatPrefixConstants
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.raid_go_action
import fr.outadoc.justchatting.shared.raid_go_message
import fr.outadoc.justchatting.shared.raid_go_title
import fr.outadoc.justchatting.utils.presentation.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RaidGoCard(
    modifier: Modifier = Modifier,
    raid: Raid.Go,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = modifier,
        onClick = {
            uriHandler.openUri(
                createChannelDeeplink(raid.targetLogin).toString(),
            )
        },
        colors =
        CardDefaults.cardColors(
            containerColor = color,
        ),
    ) {
        Row(
            modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                modifier =
                Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface),
                model = remoteImageModel(raid.targetProfileImageUrl),
                contentDescription = null,
            )

            Column(
                modifier = Modifier.weight(1f, fill = true),
            ) {
                Text(
                    text = stringResource(Res.string.raid_go_title),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text =
                    stringResource(
                        Res.string.raid_go_message,
                        "${ChatPrefixConstants.ChatterPrefix}${raid.targetDisplayName}",
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(Res.string.raid_go_action),
            )
        }
    }
}

@Preview
@Composable
private fun RaidGoCardPreview() {
    AppTheme {
        RaidGoCard(
            raid =
            Raid.Go(
                targetId = "",
                targetLogin = "",
                targetDisplayName = "HortyUnderscore",
                targetProfileImageUrl = null,
                viewerCount = 12_000,
            ),
        )
    }
}
