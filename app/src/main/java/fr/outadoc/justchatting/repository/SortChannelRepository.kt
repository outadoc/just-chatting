package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.db.SortChannelDao
import fr.outadoc.justchatting.model.offline.SortChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SortChannelRepository @Inject constructor(
    private val sortChannelDao: SortChannelDao
) {

    suspend fun getById(id: String) = withContext(Dispatchers.IO) {
        sortChannelDao.getById(id)
    }

    suspend fun save(item: SortChannel) = withContext(Dispatchers.IO) {
        sortChannelDao.insert(item)
    }
}
