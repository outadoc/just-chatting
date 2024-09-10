package fr.outadoc.justchatting.utils.permissions

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal actual fun rememberPermissionState(
    permission: String,
    onPermissionResult: (Boolean) -> Unit,
): PermissionState {
    return PermissionStateWrapper(
        com.google.accompanist.permissions.rememberPermissionState(
            permission = permission,
            onPermissionResult = onPermissionResult,
        ),
    )
}

@OptIn(ExperimentalPermissionsApi::class)
internal class PermissionStateWrapper(
    private val state: com.google.accompanist.permissions.PermissionState,
) : PermissionState {

    override val permission: String
        get() = state.permission

    override val status: PermissionStatus
        get() = when (val status = state.status) {
            is com.google.accompanist.permissions.PermissionStatus.Denied -> {
                PermissionStatus.Denied(
                    shouldShowRationale = status.shouldShowRationale,
                )
            }

            com.google.accompanist.permissions.PermissionStatus.Granted -> {
                PermissionStatus.Granted
            }
        }

    override fun launchPermissionRequest() {
        state.launchPermissionRequest()
    }
}
