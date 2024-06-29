package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews

@Composable
internal fun AutoCompleteEmoteItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    emote: Emote,
) {
    SuggestionChip(
        modifier = modifier.width(48.dp),
        onClick = onClick,
        clickLabel = emote.name,
    ) {
        EmoteItem(emote = emote)
    }
}

@Composable
internal fun AutoCompleteUserItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    chatter: Chatter,
) {
    SuggestionChip(
        modifier = modifier,
        onClick = onClick,
        clickLabel = chatter.displayName,
    ) {
        Text(text = "@${chatter.displayName}")
    }
}

@ThemePreviews
@Composable
internal fun AutoCompleteItemPreviewSimple() {
    AppTheme {
        SuggestionChip(
            onClick = {},
            clickLabel = "Lorem ipsum",
        ) {
            Text("Lorem ipsum")
        }
    }
}

@ThemePreviews
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

@Composable
internal fun SuggestionChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    clickLabel: String,
    content: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier
            .height(32.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = clickLabel,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.labelLarge,
                content = content,
            )
        }
    }
}
