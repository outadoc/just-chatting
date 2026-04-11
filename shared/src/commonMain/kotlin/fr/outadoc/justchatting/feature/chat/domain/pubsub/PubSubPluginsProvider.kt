package fr.outadoc.justchatting.feature.chat.domain.pubsub

public fun interface PubSubPluginsProvider {
    public fun get(): List<PubSubPlugin<*>>
}
