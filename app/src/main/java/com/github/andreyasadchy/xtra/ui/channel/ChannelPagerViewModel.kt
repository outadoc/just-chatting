package com.github.andreyasadchy.xtra.ui.channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.StreamQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.helix.stream.StreamsResponse
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChannelPagerViewModel @Inject constructor(private val repository: TwitchService) : ViewModel(), FollowViewModel {

    private val _channel = MutableLiveData<String>()
    val channel: LiveData<String>
        get() = _channel
    private val _stream = MutableLiveData<StreamsResponse>()
    val stream: LiveData<StreamsResponse>
        get() = _stream
    private val _streamGQL = MutableLiveData<Int?>()
    val streamGQL: MutableLiveData<Int?>
        get() = _streamGQL

    override val channelId: String
        get() {
            return _channel.value!!
        }

    override lateinit var follow: FollowLiveData

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelId, viewModelScope)
        }
    }

    fun loadStream(clientId: String?, token: String, channel: String) {
        if (_channel.value != channel) {
            _channel.value = channel
            viewModelScope.launch {
                try {
                    val stream = repository.loadStream(clientId, token, channel)
                    _stream.postValue(stream)
                } catch (e: Exception) {

                }
            }
        }
    }

    fun loadStreamGQL(clientId: String?, channel: String) {
        if (_channel.value != channel) {
            _channel.value = channel
            viewModelScope.launch {
                try {
                    val stream = apolloClient(XtraModule(), clientId).query(StreamQuery(Optional.Present(channel))).execute().data?.user?.stream?.viewersCount
                    _streamGQL.postValue(stream)
                } catch (e: Exception) {

                }
            }
        }
    }

    fun retry(clientId: String?, token: String) {
        if (_stream.value == null) {
            _channel.value?.let {
                loadStream(clientId, token, it)
            }
        }
    }

    fun retryGQL(clientId: String?) {
        if (_streamGQL.value == null) {
            _channel.value?.let {
                loadStreamGQL(clientId, it)
            }
        }
    }
}
