package com.github.andreyasadchy.xtra.ui.chat

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.pathString

class ChannelChatViewModel @Inject constructor(
    private val repository: TwitchService,
    private val localFollowsChannel: LocalFollowChannelRepository
) : ViewModel(), FollowViewModel {

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
        get() {
            return _userId.value
        }
    override val userLogin: String?
        get() {
            return _userLogin.value
        }
    override val userName: String?
        get() {
            return _userName.value
        }
    override val channelLogo: String?
        get() {
            return _profileImageURL.value
        }
    override lateinit var follow: FollowLiveData

    override fun setUser(user: User, helixClientId: String?) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(
                localFollowsChannel = localFollowsChannel,
                userId = userId,
                userLogin = userLogin,
                userName = userName,
                channelLogo = channelLogo,
                repository = repository,
                helixClientId = helixClientId,
                user = user,
                viewModelScope = viewModelScope
            )
        }
    }

    fun loadStream(
        channelId: String?,
        channelLogin: String?,
        channelName: String?,
        profileImageURL: String?,
        helixClientId: String? = null,
        helixToken: String? = null
    ) {
        if (_userId.value != channelId && channelId != null) {
            _userId.value = channelId
            _userLogin.value = channelLogin
            _userName.value = channelName
            _profileImageURL.value = profileImageURL

            viewModelScope.launch {
                try {
                    val stream = repository.loadStreamWithUser(
                        channelId = channelId,
                        helixClientId = helixClientId,
                        helixToken = helixToken
                    )
                    _stream.postValue(stream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun loadUser(
        channelId: String?,
        helixClientId: String? = null,
        helixToken: String? = null
    ) {
        if (channelId != null) {
            viewModelScope.launch {
                try {
                    val user = repository.loadUsersById(
                        ids = mutableListOf(channelId),
                        helixClientId = helixClientId,
                        helixToken = helixToken
                    )?.firstOrNull()
                    _user.postValue(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun retry(
        helixClientId: String? = null,
        helixToken: String? = null
    ) {
        if (_stream.value == null) {
            loadStream(
                channelId = _userId.value,
                channelLogin = _userLogin.value,
                channelName = _userName.value,
                profileImageURL = _profileImageURL.value,
                helixClientId = helixClientId,
                helixToken = helixToken
            )
        } else {
            if (_stream.value!!.channelUser == null) {
                loadUser(_userId.value, helixClientId, helixToken)
            }
        }
    }

    fun updateLocalUser(
        context: Context,
        user: com.github.andreyasadchy.xtra.model.helix.user.User
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (user.id == null) {
                    return@launch
                }

                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(user.channelLogo)
                        .build()

                    val result = (loader.execute(request) as SuccessResult).drawable
                    val bitmap = (result as BitmapDrawable).bitmap

                    DownloadUtils.savePng(
                        context = context,
                        folder = "profile_pics",
                        fileName = user.id,
                        bitmap = bitmap
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val downloadedLogoPath: String =
                    Path(context.filesDir.path, "profile_pics", "${user.id}.png")
                        .absolute()
                        .pathString

                localFollowsChannel.getFollowById(user.id)?.let {
                    localFollowsChannel.updateFollow(
                        it.apply {
                            user_login = user.login
                            user_name = user.display_name
                            channelLogo = downloadedLogoPath
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
