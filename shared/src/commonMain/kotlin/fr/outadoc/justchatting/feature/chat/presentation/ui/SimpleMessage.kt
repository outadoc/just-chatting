package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SimpleMessage(
    modifier: Modifier = Modifier,
    data: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.width(4.dp))
        data()
    }
}
