package fr.outadoc.justchatting.feature.preferences.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class NoopAppUpdateChecker : AppUpdateChecker {
    override val isSupported: Boolean = false

    override val state: StateFlow<AppUpdateState> = MutableStateFlow(AppUpdateState())

    override suspend fun checkForUpdate() = Unit

    override suspend fun installUpdate() = Unit
}
