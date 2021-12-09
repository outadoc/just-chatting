package com.github.andreyasadchy.xtra.ui.common.follow

import androidx.lifecycle.MutableLiveData
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.repository.TwitchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FollowLiveData(
        private val repository: TwitchService,
        private val user: LoggedIn,
        private val channelId: String?,
        private val viewModelScope: CoroutineScope,
        private val clientId: String = "") : MutableLiveData<Boolean>()  {

    init {
        viewModelScope.launch {
            try {
                val isFollowing = channelId?.let { repository.loadUserFollows(clientId, user.token, it, user.id) }
                super.setValue(isFollowing)
            } catch (e: Exception) {

            }
        }
    }

    override fun setValue(value: Boolean) {
        viewModelScope.launch {
            try {
                if (value) {

                } else {

                }
            } catch (e: Exception) {

            }
        }
    }
}