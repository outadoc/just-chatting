package fr.outadoc.justchatting.feature.onboarding.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.connectionError_logout_action
import fr.outadoc.justchatting.shared.internal.connectionError_message
import fr.outadoc.justchatting.shared.internal.connectionError_retry_action
import fr.outadoc.justchatting.shared.internal.connectionError_title
import fr.outadoc.justchatting.utils.presentation.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
public fun ConnectionErrorScreen(
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Scaffold(modifier = modifier) { insets ->
        Box(
            modifier =
                Modifier
                    .padding(insets)
                    .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .widthIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(IconSize),
                    imageVector = Icons.Default.CloudOff,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                )

                Text(
                    text = stringResource(Res.string.connectionError_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(Res.string.connectionError_message),
                    textAlign = TextAlign.Center,
                )

                Column(
                    modifier = Modifier.padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(onClick = onRetryClick) {
                        Text(text = stringResource(Res.string.connectionError_retry_action))
                    }

                    TextButton(onClick = onLogoutClick) {
                        Text(text = stringResource(Res.string.connectionError_logout_action))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun ConnectionErrorScreenPreview() {
    AppTheme {
        ConnectionErrorScreen(
            onRetryClick = {},
            onLogoutClick = {},
        )
    }
}

private val IconSize = 64.dp
