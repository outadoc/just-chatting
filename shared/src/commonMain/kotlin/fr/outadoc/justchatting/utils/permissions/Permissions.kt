package fr.outadoc.justchatting.utils.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

internal interface PermissionState {
    val permission: String
    val status: PermissionStatus
    fun launchPermissionRequest()
}

@Stable
internal sealed interface PermissionStatus {
    data object Granted : PermissionStatus
    data class Denied(
        val shouldShowRationale: Boolean,
    ) : PermissionStatus
}

@Composable
internal expect fun rememberPermissionState(
    permission: String,
    onPermissionResult: (Boolean) -> Unit = {},
): PermissionState

internal val PermissionStatus.isGranted: Boolean
    get() = this == PermissionStatus.Granted

internal val PermissionStatus.shouldShowRationale: Boolean
    get() = when (this) {
        PermissionStatus.Granted -> false
        is PermissionStatus.Denied -> shouldShowRationale
    }
