package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.preferences.presentation.AppUpdateState
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.update_dialog_cancel
import fr.outadoc.justchatting.shared.internal.update_dialog_confirm
import fr.outadoc.justchatting.shared.internal.update_dialog_downloading
import fr.outadoc.justchatting.shared.internal.update_dialog_message
import fr.outadoc.justchatting.shared.internal.update_dialog_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UpdateAvailableDialog(
    modifier: Modifier = Modifier,
    state: AppUpdateState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            if (!state.isDownloading) {
                onDismiss()
            }
        },
        title = { Text(text = stringResource(Res.string.update_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        Res.string.update_dialog_message,
                        state.availableVersion.orEmpty(),
                    ),
                )

                if (state.isDownloading) {
                    val progress = state.downloadProgress
                    if (progress != null) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = { progress },
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isDownloading,
            ) {
                Text(text = stringResource(Res.string.update_dialog_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !state.isDownloading,
            ) {
                Text(
                    text = if (state.isDownloading) {
                        stringResource(Res.string.update_dialog_downloading)
                    } else {
                        stringResource(Res.string.update_dialog_confirm)
                    },
                )
            }
        },
    )
}
