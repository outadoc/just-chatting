package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.recent.domain.model.RecentEmote
import kotlinx.coroutines.flow.Flow

internal class GetRecentEmotesUseCase(
    private val recentEmotesApi: RecentEmotesApi,
) {
    operator fun invoke(): Flow<List<RecentEmote>> {
        return recentEmotesApi.getAll()
    }
}
