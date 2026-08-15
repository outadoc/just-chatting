package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Lets a message sent through [DemoTwitchRepository.sendChatMessage] be echoed back into
 * [DemoChatRepository]'s event flow, mimicking the round-trip a real IRC connection would do.
 */
internal class DemoChatBus {
    private val _events = MutableSharedFlow<Pair<String, ChatEvent>>(extraBufferCapacity = 64)
    val events: SharedFlow<Pair<String, ChatEvent>> = _events.asSharedFlow()

    fun post(
        channelId: String,
        event: ChatEvent,
    ) {
        _events.tryEmit(channelId to event)
    }
}
