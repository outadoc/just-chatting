package com.github.andreyasadchy.xtra.ui.main

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.validate
import com.github.andreyasadchy.xtra.repository.AuthPreferencesRepository
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.toast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: TwitchService,
    private val authRepository: AuthRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _userToShow = MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>()
    val userToShow: MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>
        get() = _userToShow

    val currentUser = userPreferencesRepository.user.asLiveData()

    fun loadUser(login: String? = null) {
        _userToShow.value = null
        viewModelScope.launch {
            try {
                val user = login?.let {
                    repository.loadUsersByLogin(listOf(login))
                }?.firstOrNull()

                _userToShow.postValue(user)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun validate(activity: Activity) {
        viewModelScope.launch {
            val user = userPreferencesRepository.user.first()
            val helixClientId = authPreferencesRepository.helixClientId.first()

            if (user is User.NotValidated) {
                try {
                    val token = user.helixToken
                    if (!token.isNullOrBlank()) {
                        val response = authRepository.validate(token)
                        val validatedUser: User.LoggedIn? = user.validate()
                        if (response?.clientId == helixClientId && validatedUser != null) {
                            userPreferencesRepository.updateUser(validatedUser)
                        } else {
                            throw IllegalStateException("401")
                        }
                    }
                } catch (e: Exception) {
                    if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
                        userPreferencesRepository.updateUser(null)
                        activity.toast(R.string.token_expired)
                        activity.startActivityForResult(
                            Intent(activity, LoginActivity::class.java),
                            2
                        )
                    }
                }
            }

            TwitchApiHelper.checkedValidation = true
        }
    }
}
