package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object DetailsListItemDefaults {
    /**
     * Height of the leading content, e.g. an emote or an avatar.
     */
    val LeadingContentHeight: Dp = 40.dp
}

/**
 * A compact list item for the lists shown in details sheets, such as the emotes used in a
 * message or the channels in a shared chat.
 */
@Composable
internal fun DetailsListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable () -> Unit,
    headlineContent: @Composable () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ).padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.height(DetailsListItemDefaults.LeadingContentHeight),
            contentAlignment = Alignment.Center,
        ) {
            leadingContent()
        }

        Box(
            modifier = Modifier.weight(1f),
        ) {
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                headlineContent()
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}
