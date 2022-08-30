package fr.outadoc.justchatting.ui.view.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageClickedViewModel @Inject constructor(private val repository: TwitchService) :
    BaseViewModel() {

    private val user = MutableLiveData<User?>()
    private var isLoading = false

    fun loadUser(channelId: String): MutableLiveData<User?> {
        if (user.value == null && !isLoading) {
            isLoading = true
            viewModelScope.launch {
                try {
                    user.postValue(
                        repository.loadUsersById(listOf(channelId))
                            ?.firstOrNull()
                    )
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
