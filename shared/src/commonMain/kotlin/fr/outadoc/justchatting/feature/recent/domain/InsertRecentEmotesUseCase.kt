package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.recent.domain.model.RecentEmote

internal class InsertRecentEmotesUseCase(
    private val recentEmotesApi: RecentEmotesApi,
) {
    operator fun invoke(emotes: Collection<RecentEmote>) {
        recentEmotesApi.insertAll(emotes)
    }
}
