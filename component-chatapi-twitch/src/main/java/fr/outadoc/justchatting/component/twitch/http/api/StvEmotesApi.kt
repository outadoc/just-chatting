package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.StvEmote

interface StvEmotesApi {

    suspend fun getGlobalStvEmotes(): List<StvEmote>

    suspend fun getStvEmotes(channelId: String): List<StvEmote>
}
