package fr.outadoc.justchatting.component.chatapi.common.eventsub

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent

interface EventSubPlugin<T : Any> {

    val subscriptionType: String
    fun parseMessage(message: String): ChatEvent?
}
