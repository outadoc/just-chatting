package fr.outadoc.justchatting.component.twitch.websocket.eventsub.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventSubMessageWithEvent<T : Any>(
    @SerialName("payload")
    val payload: Payload<T>,
) {
    @Serializable
    data class Payload<T>(
        @SerialName("event")
        val event: T,
    )
}
