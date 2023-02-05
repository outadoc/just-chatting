package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ChatMessageCensoredBody(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Text("<removed>")
    }
}
