package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.StvEmoteResponse

interface StvEmotesApi {
    suspend fun getGlobalStvEmotes(): Result<StvEmoteResponse>
}
