package fr.outadoc.justchatting.feature.chat.domain.pubsub

internal fun interface PubSubPluginsProvider {
    public fun get(): List<PubSubPlugin<*>>
}
