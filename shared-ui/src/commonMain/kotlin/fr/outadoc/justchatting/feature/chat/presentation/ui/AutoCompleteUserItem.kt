package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.utils.presentation.AppTheme

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

@Preview
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
