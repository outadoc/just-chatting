package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub

import kotlin.reflect.KClass

interface PubSubPlugin<T : Any> {

    val model: KClass<T>

    fun getTopic(channelId: String)
    fun onReceive(message: T)
}