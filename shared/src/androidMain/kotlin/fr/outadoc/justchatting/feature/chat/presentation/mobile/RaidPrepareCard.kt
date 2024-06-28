package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.component.chatapi.common.Raid
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews

@Composable
internal fun RaidPrepareCard(
    modifier: Modifier = Modifier,
    raid: Raid.Preparing,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    Card(
        modifier = modifier,
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
                    text = stringResource(MR.strings.raid_prepare_title),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = stringResource(
                        MR.strings.raid_prepare_message,
                        raid.targetDisplayName,
                        MR.plurals.viewers.format(
                            number = raid.viewerCount,
                            raid.viewerCount.formatNumber(),
                        ).toString(LocalContext.current),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun RaidPrepareCardPreview() {
    AppTheme {
        RaidPrepareCard(
            raid = Raid.Preparing(
                targetId = "",
                targetLogin = "",
                targetDisplayName = "HortyUnderscore",
                targetProfileImageUrl = null,
                viewerCount = 12_000,
            ),
        )
    }
}
