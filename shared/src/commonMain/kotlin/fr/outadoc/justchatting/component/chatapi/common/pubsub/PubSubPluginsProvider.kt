package fr.outadoc.justchatting.component.chatapi.common.pubsub

internal fun interface PubSubPluginsProvider {
    fun get(): List<PubSubPlugin<*>>
}
