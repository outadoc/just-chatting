package fr.outadoc.justchatting.component.chatapi.common.eventsub

fun interface EventSubPluginsProvider {
    fun get(): List<EventSubPlugin<*>>
}
