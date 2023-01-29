package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin

fun interface EventSubPluginsProvider {
    fun get(): List<EventSubPlugin<*>>
}
