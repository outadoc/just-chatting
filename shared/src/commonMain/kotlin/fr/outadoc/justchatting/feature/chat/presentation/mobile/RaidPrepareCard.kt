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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.raid_prepare_message
import fr.outadoc.justchatting.shared.raid_prepare_title
import fr.outadoc.justchatting.shared.viewers
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.asString
import fr.outadoc.justchatting.utils.presentation.formatNumber
import fr.outadoc.justchatting.utils.resources.desc2
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                    text = stringResource(Res.string.raid_prepare_title),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = stringResource(
                        Res.string.raid_prepare_message,
                        raid.targetDisplayName,
                        Res.plurals.viewers
                            .desc2(
                                number = raid.viewerCount,
                                raid.viewerCount.formatNumber(),
                            )
                            .localized(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview
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
