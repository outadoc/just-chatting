package com.github.andreyasadchy.xtra.ui.view.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageClickedViewModel @Inject constructor(private val repository: TwitchService) : BaseViewModel() {

    private val user = MutableLiveData<User>()
    private var isLoading = false

    fun loadUser(clientId: String?, token: String?, channelName: String): LiveData<User> {
        if (user.value == null && !isLoading) {
            isLoading = true
            viewModelScope.launch {
                try {
                    val u = repository.loadUserByLogin(clientId, token, channelName)
                    user.postValue(u)
                } catch (e: Exception) {
                    _errors.postValue(e)
                } finally {
                    isLoading = false
                }
            }
        }
        return user
    }
}