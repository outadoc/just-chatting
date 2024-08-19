package fr.outadoc.justchatting.feature.pronouns.data.db

import fr.outadoc.justchatting.data.db.PronounQueries
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext

internal class LocalPronounsDb(
    private val db: PronounQueries,
) : LocalPronounsApi {

    override suspend fun arePronounsSynced(): Boolean {
        return withContext(DispatchersProvider.io) {
            db.getPronouns().executeAsList().isNotEmpty()
        }
    }


}
