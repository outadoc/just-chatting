package fr.outadoc.justchatting.ui.chat

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.ui.theme.AppTheme
import fr.outadoc.justchatting.ui.view.emotes.EmoteItem

@Composable
fun ChatAutoCompleteItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    item: AutoCompleteItem,
    animateEmotes: Boolean = true
) {
    when (item) {
        is AutoCompleteItem.Emote -> {
            AutoCompleteEmoteItem(
                modifier = modifier,
                emote = item.emote,
                onClick = onClick,
                animateEmotes = animateEmotes
            )
        }

        is AutoCompleteItem.User -> {
            AutoCompleteUserItem(
                modifier = modifier,
                onClick = onClick,
                username = item.username
            )
        }
    }
}

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
            modifier = Modifier.size(32.dp),
            emote = emote,
            animateEmotes = animateEmotes
        )
    }
}

@Composable
fun AutoCompleteUserItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    username: String
) {
    AutoCompleteItemContent(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(text = "@$username")
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
    Chip(
        modifier = modifier,
        onClick = onClick,
        content = content
    )
}
