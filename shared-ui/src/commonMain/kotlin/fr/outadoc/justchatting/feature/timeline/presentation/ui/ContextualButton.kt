package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun ContextualButton(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        icon()

        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))

        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
