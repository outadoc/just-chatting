package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteSetItem

@Composable
fun EmoteHeader(
    modifier: Modifier = Modifier,
    header: EmoteSetItem.Header
) {
    Column(modifier = modifier) {
        header.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }

        header.source?.let { source ->
            Text(
                text = source,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
