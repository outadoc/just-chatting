package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.utils.presentation.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun AutoCompleteEmoteItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    emote: Emote,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val tooltipState = rememberBasicTooltipState(isPersistent = false)

    BasicTooltipBox(
        positionProvider = tooltipPosition,
        state = tooltipState,
        tooltip = {
            ElevatedCard {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = emote.name,
                )
            }
        },
    ) {
        SuggestionChip(
            modifier = modifier.width(48.dp),
            onClick = onClick,
            clickLabel = emote.name,
        ) {
            EmoteItem(emote = emote)
        }
    }
}

@Preview
@Composable
internal fun AutoCompleteItemPreviewIcon() {
    AppTheme {
        SuggestionChip(
            onClick = {},
            clickLabel = "Lorem ipsum",
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
        }
    }
}
