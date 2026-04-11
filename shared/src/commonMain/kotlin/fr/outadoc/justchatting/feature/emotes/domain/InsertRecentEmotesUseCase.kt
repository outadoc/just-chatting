package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote

public class InsertRecentEmotesUseCase(
    private val recentEmotesApi: RecentEmotesApi,
) {
    public operator fun invoke(emotes: Collection<RecentEmote>) {
        recentEmotesApi.insertAll(emotes)
    }
}
