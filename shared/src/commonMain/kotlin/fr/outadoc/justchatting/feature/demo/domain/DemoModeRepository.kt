package fr.outadoc.justchatting.feature.demo.domain

import kotlinx.coroutines.flow.StateFlow

internal interface DemoModeRepository {
    val isDemoMode: StateFlow<Boolean>

    fun setDemoMode(enabled: Boolean)
}
