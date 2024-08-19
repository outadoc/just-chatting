package fr.outadoc.justchatting.feature.pronouns.data.db

internal interface LocalPronounsApi {
    suspend fun arePronounsSynced(): Boolean
}
