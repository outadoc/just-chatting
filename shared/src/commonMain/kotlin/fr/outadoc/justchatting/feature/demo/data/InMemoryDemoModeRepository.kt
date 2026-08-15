package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class InMemoryDemoModeRepository : DemoModeRepository {
    private val _isDemoMode = MutableStateFlow(false)
    override val isDemoMode: StateFlow<Boolean> = _isDemoMode

    override fun setDemoMode(enabled: Boolean) {
        _isDemoMode.value = enabled
    }
}
