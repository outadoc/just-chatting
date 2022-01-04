package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import kotlinx.coroutines.CoroutineScope

class FollowedChannelsDataSource(
    private val localFollows: LocalFollowRepository,
    private val clientId: String?,
    private val userToken: String?,
    private val userId: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Follow>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Follow>()
            for (i in localFollows.loadFollows()) {
                list.add(Follow(to_id = i.user_id, to_login = i.user_login, to_name = i.user_name, profileImageURL = i.channelLogo, followLocal = true))
            }
            if (userId != "") {
                val get = api.getFollowedChannels(clientId, userToken, userId, params.requestedLoadSize, offset)
                for (i in get.data) {
                    val item = list.find { it.to_id == i.to_id }
                    if (item == null) {
                        i.profileImageURL = i.to_id?.let { api.getUserById(clientId, userToken, i.to_id).data?.first()?.profile_image_url }
                        list.add(i)
                    } else {
                        item.followTwitch = true
                    }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        loadRange(params, callback) {
            val list = mutableListOf<Follow>()
            if (userId != "" && offset != null && offset != "") {
                val get = api.getFollowedChannels(clientId, userToken, userId, params.loadSize, offset)
                for (i in get.data) {
                    val item = list.find { it.to_id == i.to_id }
                    if (item == null) {
                        i.profileImageURL = i.to_id?.let { api.getUserById(clientId, userToken, i.to_id).data?.first()?.profile_image_url }
                        list.add(i)
                    } else {
                        item.followTwitch = true
                    }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val localFollows: LocalFollowRepository,
        private val clientId: String?,
        private val userToken: String?,
        private val userId: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
                FollowedChannelsDataSource(localFollows, clientId, userToken, userId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
