package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SimpleMessage(
    modifier: Modifier = Modifier,
    data: @Composable () -> Unit,
) {
    Row {
        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = modifier.padding(
                horizontal = 4.dp,
                vertical = 6.dp,
            ),
        ) {
            data()
        }
    }
}
