package com.github.andreyasadchy.xtra.ui.channel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class ChannelPagerViewModel @Inject constructor(
    private val repository: TwitchService,
    private val localFollows: LocalFollowRepository,
    private val offlineRepository: OfflineRepository) : ViewModel(), FollowViewModel {

    private val _stream = MutableLiveData<Stream?>()
    val stream: MutableLiveData<Stream?>
        get() = _stream
    private val _user = MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>()
    val user: MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>
        get() = _user

    private val _userId = MutableLiveData<String?>()
    private val _userLogin = MutableLiveData<String?>()
    private val _userName = MutableLiveData<String?>()
    private val _profileImageURL = MutableLiveData<String?>()
    override val userId: String?
        get() { return _userId.value }
    override val userLogin: String?
        get() { return _userLogin.value }
    override val userName: String?
        get() { return _userName.value }
    override val channelLogo: String?
        get() { return _profileImageURL.value }
    override lateinit var follow: FollowLiveData

    override fun setUser(user: User, clientId: String?) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(localFollows, userId, userLogin, userName, channelLogo, repository, clientId, user, viewModelScope)
        }
    }

    fun loadStream(useHelix: Boolean, clientId: String?, token: String? = null, channelId: String?, channelLogin: String?, channelName: String?, profileImageURL: String?) {
        if (_userId.value != channelId && channelId != null) {
            _userId.value = channelId
            _userLogin.value = channelLogin
            _userName.value = channelName
            _profileImageURL.value = profileImageURL
            viewModelScope.launch {
                try {
                    val stream = if (useHelix) {
                        repository.loadStream(clientId, token, channelId)
                    } else {
                        repository.loadStreamWithUserGQLQuery(clientId, channelId)
                    }
                    _stream.postValue(stream)
                } catch (e: Exception) {

                }
            }
        }
    }

    fun loadUser(useHelix: Boolean, clientId: String?, token: String? = null, channelId: String?) {
        if (useHelix && channelId != null) {
            viewModelScope.launch {
                try {
                    val user = repository.loadUserById(clientId, token, channelId)
                    _user.postValue(user)
                } catch (e: Exception) {

                }
            }
        }
    }

    fun retry(useHelix: Boolean, clientId: String?, token: String? = null) {
        if (_stream.value == null) {
            loadStream(useHelix, clientId, token, _userId.value, _userLogin.value, _userName.value, _profileImageURL.value)
        }
        if (useHelix && _user.value == null) {
            loadUser(useHelix, clientId, token, _userId.value)
        }
    }

    fun updateLocalUser(context: Context, user: com.github.andreyasadchy.xtra.model.helix.user.User) {
        GlobalScope.launch {
            try {
                if (user.id != null) {
                    try {
                        Glide.with(context)
                            .asBitmap()
                            .load(user.channelLogo)
                            .into(object: CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {

                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    DownloadUtils.savePng(context, "profile_pics", user.id, resource)
                                }
                            })
                    } catch (e: Exception) {

                    }
                    val downloadedLogo = File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${user.id}.png").absolutePath
                    localFollows.getFollowById(user.id)?.let { localFollows.updateFollow(it.apply {
                        user_login = user.login
                        user_name = user.display_name
                        channelLogo = downloadedLogo }) }
                    for (i in offlineRepository.getVideosByUserId(user.id.toInt())) {
                        offlineRepository.updateVideo(i.apply {
                            channelLogin = user.login
                            channelName = user.display_name
                            channelLogo = downloadedLogo })
                    }
                }
            } catch (e: Exception) {

            }
        }
    }
}
