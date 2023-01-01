package fr.outadoc.justchatting.feature.chat.data.emotes

import kotlinx.collections.immutable.ImmutableSet

interface EmoteListSource {

    suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
        userId: String
    ): ImmutableSet<EmoteSetItem>
}
