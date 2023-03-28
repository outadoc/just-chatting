package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews

@Composable
fun AutoCompleteEmoteItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    emote: Emote,
) {
    AutoCompleteItemContent(
        modifier = modifier,
        onClick = onClick,
    ) {
        EmoteItem(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp),
            emote = emote,
        )
    }
}

@Composable
fun AutoCompleteUserItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    chatter: Chatter,
) {
    AutoCompleteItemContent(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(text = "@${chatter.displayName}")
    }
}

@ThemePreviews
@Composable
fun AutoCompleteItemPreviewSimple() {
    AppTheme {
        AutoCompleteItemContent(onClick = {}) {
            Text("Lorem ipsum")
        }
    }
}

@ThemePreviews
@Composable
fun AutoCompleteItemPreviewIcon() {
    AppTheme {
        AutoCompleteItemContent(onClick = {}) {
            Icon(Icons.Default.Person, contentDescription = null)
        }
    }
}

@Composable
fun AutoCompleteItemContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    ElevatedSuggestionChip(
        modifier = modifier,
        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        label = content,
    )
}
