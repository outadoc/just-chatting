package com.github.andreyasadchy.xtra.ui.main

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotValidated
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.util.Event
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.toast
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class MainViewModel @Inject constructor(
        application: Application,
        private val repository: TwitchService,
        private val authRepository: AuthRepository,
        private val offlineRepository: OfflineRepository) : ViewModel() {

    private val _isNetworkAvailable = MutableLiveData<Event<Boolean>>()
    val isNetworkAvailable: LiveData<Event<Boolean>>
        get() = _isNetworkAvailable

    var isPlayerMaximized = false
        private set

    var isPlayerOpened = false
        private set

    init {
        offlineRepository.resumeDownloads(application, false)
    }

    fun onMaximize() {
        isPlayerMaximized = true
    }

    fun onMinimize() {
        isPlayerMaximized = false
    }

    fun onPlayerStarted() {
        isPlayerOpened = true
        isPlayerMaximized = true
    }

    fun onPlayerClosed() {
        isPlayerOpened = false
        isPlayerMaximized = false
    }

    fun setNetworkAvailable(available: Boolean) {
        if (_isNetworkAvailable.value?.peekContent() != available) {
            _isNetworkAvailable.value = Event(available)
        }
    }

    fun validate(helixClientId: String?, gqlClientId: String?, activity: Activity) {
        val user = User.get(activity)
        if (user is NotValidated) {
            viewModelScope.launch {
                try {
                    if (!user.helixToken.isNullOrBlank()) {
                        val response = authRepository.validate(TwitchApiHelper.addTokenPrefixHelix(user.helixToken))
                        if (response?.clientId == helixClientId) {
                            User.validated()
                        } else {
                            throw IllegalStateException("401")
                        }
                    }
                    if (!user.gqlToken.isNullOrBlank()) {
                        val response = authRepository.validate(TwitchApiHelper.addTokenPrefixGQL(user.gqlToken))
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
                        activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 2)
                    }
                }
            }
        }
        TwitchApiHelper.checkedValidation = true
    }
}