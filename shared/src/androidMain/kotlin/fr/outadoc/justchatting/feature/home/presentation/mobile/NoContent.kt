package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.nothing_here
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ScreenPreviews

@ScreenPreviews
@Composable
fun NoContentPreview() {
    AppTheme {
        NoContent()
    }
}

@Composable
fun NoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.nothing_here),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
