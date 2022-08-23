package com.github.andreyasadchy.xtra.ui.main

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.validate
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.toast
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: TwitchService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>()
    val user: MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>
        get() = _user

    fun loadUser(
        login: String? = null,
        helixClientId: String? = null,
        helixToken: String? = null
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
                e.printStackTrace()
            }
        }
    }

    fun validate(helixClientId: String?, activity: Activity) {
        val user = User.get(activity.applicationContext)
        if (user is User.NotValidated) {
            viewModelScope.launch {
                try {
                    val token = user.helixToken
                    if (!token.isNullOrBlank()) {
                        val response = authRepository.validate(
                            TwitchApiHelper.addTokenPrefixHelix(token)
                        )

                        val validatedUser = user.validate()
                        if (response?.clientId == helixClientId && validatedUser != null) {
                            User.set(activity.applicationContext, validatedUser)
                        } else {
                            throw IllegalStateException("401")
                        }
                    }
                } catch (e: Exception) {
                    if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
                        User.set(activity.applicationContext, null)
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
