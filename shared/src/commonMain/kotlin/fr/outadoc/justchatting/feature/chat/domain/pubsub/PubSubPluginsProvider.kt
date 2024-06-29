package fr.outadoc.justchatting.feature.chat.domain.pubsub

internal fun interface PubSubPluginsProvider {
    fun get(): List<PubSubPlugin<*>>
}
