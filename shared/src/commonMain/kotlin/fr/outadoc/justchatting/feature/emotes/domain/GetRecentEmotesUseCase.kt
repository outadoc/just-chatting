package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import kotlinx.coroutines.flow.Flow

public class GetRecentEmotesUseCase(
    private val recentEmotesApi: RecentEmotesApi,
) {
    public operator fun invoke(): Flow<List<RecentEmote>> = recentEmotesApi.getAll()
}
