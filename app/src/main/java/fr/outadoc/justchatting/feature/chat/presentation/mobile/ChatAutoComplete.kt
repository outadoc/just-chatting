package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.twitch.model.Chatter
import fr.outadoc.justchatting.component.twitch.model.Emote
import fr.outadoc.justchatting.feature.mainnavigation.presentation.AppTheme

@Composable
fun AutoCompleteEmoteItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    animateEmotes: Boolean = true,
    emote: Emote
) {
    AutoCompleteItemContent(
        modifier = modifier,
        onClick = onClick
    ) {
        EmoteItem(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp),
            emote = emote,
            animateEmotes = animateEmotes
        )
    }
}

@Composable
fun AutoCompleteUserItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    chatter: Chatter
) {
    AutoCompleteItemContent(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(text = "@${chatter.name}")
    }
}

@Preview
@Composable
fun AutoCompleteItemPreviewSimple() {
    AppTheme {
        AutoCompleteItemContent(onClick = {}) {
            Text("Lorem ipsum")
        }
    }
}

@Preview
@Composable
fun AutoCompleteItemPreviewIcon() {
    AppTheme {
        AutoCompleteItemContent(onClick = {}) {
            Icon(Icons.Default.Person, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AutoCompleteItemContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Chip(
        modifier = modifier,
        colors = ChipDefaults.chipColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        content = content
    )
}
