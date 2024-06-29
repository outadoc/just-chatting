package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.chat.presentation.ChatPrefixConstants
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
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
                createChannelDeeplink(raid.targetLogin),
            )
        },
        colors = CardDefaults.cardColors(
            containerColor = color,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                modifier = Modifier
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
                    text = stringResource(MR.strings.raid_go_title),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = stringResource(
                        MR.strings.raid_go_message,
                        "${ChatPrefixConstants.ChatterPrefix}${raid.targetDisplayName}",
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = stringResource(MR.strings.raid_go_action),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RaidGoCardPreview() {
    AppTheme {
        RaidGoCard(
            raid = Raid.Go(
                targetId = "",
                targetLogin = "",
                targetDisplayName = "HortyUnderscore",
                targetProfileImageUrl = null,
                viewerCount = 12_000,
            ),
        )
    }
}
