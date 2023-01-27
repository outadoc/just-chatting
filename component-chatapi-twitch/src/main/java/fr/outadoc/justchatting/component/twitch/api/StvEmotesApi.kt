package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.StvEmote

interface StvEmotesApi {

    suspend fun getGlobalStvEmotes(): List<StvEmote>

    suspend fun getStvEmotes(channelId: String): List<StvEmote>
}
