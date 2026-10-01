package fr.outadoc.justchatting.feature.chat.domain.eventsub

internal fun interface EventSubPluginsProvider {
    fun get(): List<EventSubPlugin>
}
