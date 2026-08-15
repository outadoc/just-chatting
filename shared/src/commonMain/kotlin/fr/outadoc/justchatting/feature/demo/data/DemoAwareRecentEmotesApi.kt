package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.emotes.data.db.RecentEmotesDb
import fr.outadoc.justchatting.feature.emotes.domain.RecentEmotesApi
import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import kotlinx.coroutines.flow.Flow

internal class DemoAwareRecentEmotesApi(
    private val demoModeRepository: DemoModeRepository,
    private val real: Lazy<RecentEmotesDb>,
    private val demo: DemoRecentEmotesApi,
) : RecentEmotesApi {
    private fun current(): RecentEmotesApi = if (demoModeRepository.isDemoMode.value) demo else real.value

    override fun getAll(): Flow<List<RecentEmote>> = current().getAll()

    override fun insertAll(emotes: Collection<RecentEmote>) {
        current().insertAll(emotes)
    }
}
