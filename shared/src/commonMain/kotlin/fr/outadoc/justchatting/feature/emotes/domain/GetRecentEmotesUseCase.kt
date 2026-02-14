package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import kotlinx.coroutines.flow.Flow

internal class GetRecentEmotesUseCase(
    private val recentEmotesApi: RecentEmotesApi,
) {
    operator fun invoke(): Flow<List<RecentEmote>> = recentEmotesApi.getAll()
}
