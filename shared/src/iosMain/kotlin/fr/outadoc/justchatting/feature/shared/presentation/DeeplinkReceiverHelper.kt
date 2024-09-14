package fr.outadoc.justchatting.feature.shared.presentation

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public class DeeplinkReceiverHelper : KoinComponent {
    private val instance: DeeplinkReceiver by inject()
    public fun getInstance(): DeeplinkReceiver = instance
}
