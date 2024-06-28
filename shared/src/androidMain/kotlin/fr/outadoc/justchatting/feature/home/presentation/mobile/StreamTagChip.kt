package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import fr.outadoc.justchatting.utils.ui.LabelChip

@Composable
internal fun StreamTagChip(
    tag: String,
    modifier: Modifier = Modifier,
) {
    LabelChip(modifier = modifier) {
        Text(
            tag,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}
