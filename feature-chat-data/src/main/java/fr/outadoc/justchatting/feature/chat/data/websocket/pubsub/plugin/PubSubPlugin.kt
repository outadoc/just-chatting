package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.plugin

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import kotlin.reflect.KClass

interface PubSubPlugin<T : Any> {

    val model: KClass<T>

    fun getTopic(channelId: String): String
    fun parseMessage(message: T): ChatCommand?
}
