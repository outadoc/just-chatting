package fr.outadoc.justchatting.component.chatapi.common.pubsub

fun interface PubSubPluginsProvider {
    fun get(): List<PubSubPlugin<*>>
}
