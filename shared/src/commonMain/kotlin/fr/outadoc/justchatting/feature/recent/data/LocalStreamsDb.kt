package fr.outadoc.justchatting.feature.recent.data

import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.feature.recent.domain.LocalStreamsApi
import kotlinx.datetime.Clock

internal class LocalStreamsDb(
    private val streamQueries: StreamQueries,
    private val clock: Clock,
) : LocalStreamsApi {

}
