package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSource
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.resources.desc

internal class DemoEmoteListSource : EmoteListSource<List<EmoteSetItem>> {
    override suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): Result<List<EmoteSetItem>> =
        Result.success(
            flatListOf(
                EmoteSetItem.Header(title = "Demo".desc(), source = null),
                DemoData.setEmotes.map { emote -> EmoteSetItem.Emote(emote) },
            ),
        )
}
