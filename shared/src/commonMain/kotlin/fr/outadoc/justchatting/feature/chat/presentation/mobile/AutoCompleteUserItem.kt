package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.utils.presentation.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

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
