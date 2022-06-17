package com.github.andreyasadchy.xtra.ui.main

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotValidated
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.util.Event
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.toast
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: TwitchService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isNetworkAvailable = MutableLiveData<Event<Boolean>>()
    val isNetworkAvailable: LiveData<Event<Boolean>>
        get() = _isNetworkAvailable

    private val _video = MutableLiveData<Video?>()
    val video: MutableLiveData<Video?>
        get() = _video
    private val _clip = MutableLiveData<Clip?>()
    val clip: MutableLiveData<Clip?>
        get() = _clip
    private val _user = MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>()
    val user: MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>
        get() = _user

    fun setNetworkAvailable(available: Boolean) {
        if (_isNetworkAvailable.value?.peekContent() != available) {
            _isNetworkAvailable.value = Event(available)
        }
    }

    fun loadUser(
        login: String? = null,
        helixClientId: String? = null,
        helixToken: String? = null,
        gqlClientId: String? = null
    ) {
        _user.value = null
        viewModelScope.launch {
            try {
                val user = login?.let {
                    repository.loadUsersByLogin(
                        mutableListOf(login),
                        helixClientId,
                        helixToken
                    )
                }?.firstOrNull()
                _user.postValue(user)
            } catch (e: Exception) {
            }
        }
    }

    fun validate(helixClientId: String?, gqlClientId: String?, activity: Activity) {
        val user = User.get(activity)
        if (user is NotValidated) {
            viewModelScope.launch {
                try {
                    if (!user.helixToken.isNullOrBlank()) {
                        val response =
                            authRepository.validate(TwitchApiHelper.addTokenPrefixHelix(user.helixToken))
                        if (response?.clientId == helixClientId) {
                            User.validated()
                        } else {
                            throw IllegalStateException("401")
                        }
                    }
                    if (!user.gqlToken.isNullOrBlank()) {
                        val response =
                            authRepository.validate(TwitchApiHelper.addTokenPrefixGQL(user.gqlToken))
                        if (response?.clientId == gqlClientId) {
                            User.validated()
                        } else {
                            throw IllegalStateException("401")
                        }
                    }
                } catch (e: Exception) {
                    if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
                        User.set(activity, null)
                        activity.toast(R.string.token_expired)
                        activity.startActivityForResult(
                            Intent(activity, LoginActivity::class.java),
                            2
                        )
                    }
                }
            }
        }
        TwitchApiHelper.checkedValidation = true
    }
}
