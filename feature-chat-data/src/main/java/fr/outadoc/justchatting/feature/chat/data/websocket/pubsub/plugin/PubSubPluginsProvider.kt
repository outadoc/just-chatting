package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.plugin

fun interface PubSubPluginsProvider {
    fun get(): List<PubSubPlugin<*>>
}
