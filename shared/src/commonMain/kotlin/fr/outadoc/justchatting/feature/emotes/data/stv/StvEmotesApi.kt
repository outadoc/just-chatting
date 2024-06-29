package fr.outadoc.justchatting.feature.emotes.data.stv

import fr.outadoc.justchatting.feature.emotes.data.stv.model.StvEmoteResponse

interface StvEmotesApi {
    suspend fun getGlobalStvEmotes(): Result<StvEmoteResponse>
}
