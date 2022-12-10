package fr.outadoc.justchatting.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.ui.theme.AppTheme

@Preview
@Composable
fun ChatAutoCompleteRowPreview() {
    AppTheme {
        val items = listOf(
            AutoCompleteItem.User("BagheraJones"),
            AutoCompleteItem.User("HortyUnderscore")
        )

        ChatAutoCompleteRow(
            onClick = {},
            items = items
        )
    }
}

@Composable
fun ChatAutoCompleteRow(
    modifier: Modifier = Modifier,
    onClick: (AutoCompleteItem) -> Unit,
    items: List<AutoCompleteItem>
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            contentType = { item ->
                when (item) {
                    is AutoCompleteItem.Emote -> 0
                    is AutoCompleteItem.User -> 1
                }
            }
        ) { item ->
            ChatAutoCompleteItem(
                onClick = { onClick(item) },
                item = item
            )
        }
    }
}
