package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand

interface EventSubPlugin<T : Any> {

    fun getTopic(channelId: String): String
    fun parseMessage(message: String): ChatCommand?
}
