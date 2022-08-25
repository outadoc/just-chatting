package com.github.andreyasadchy.xtra.ui.chat

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.ChatPreferencesRepository
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.pathString

class ChannelChatViewModel @Inject constructor(
    private val repository: TwitchService,
    private val localFollowsChannel: LocalFollowChannelRepository,
    chatPreferencesRepository: ChatPreferencesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel(), FollowViewModel {

    private val _stream = MutableLiveData<Stream?>()
    val stream: MutableLiveData<Stream?>
        get() = _stream

    private val _loadedUser =
        MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>()

    val loadedUser: MutableLiveData<com.github.andreyasadchy.xtra.model.helix.user.User?>
        get() = _loadedUser

    val user = userPreferencesRepository.user.asLiveData()

    private val _userId = MutableLiveData<String?>()
    private val _userLogin = MutableLiveData<String?>()
    private val _userName = MutableLiveData<String?>()
    private val _profileImageURL = MutableLiveData<String?>()

    override val userId: String?
        get() = _userId.value

    override val userLogin: String?
        get() = _userLogin.value

    override val userName: String?
        get() = _userName.value

    override val channelLogo: String?
        get() = _profileImageURL.value

    override lateinit var follow: FollowLiveData

    data class State(
        val showTimestamps: Boolean = false,
        val animateEmotes: Boolean = true,
    )

    val state = combine(
        chatPreferencesRepository.showTimestamps,
        chatPreferencesRepository.animateEmotes
    ) { showTimestamps, animateEmotes ->
        State(
            showTimestamps = showTimestamps,
            animateEmotes = animateEmotes
        )
    }.asLiveData()

    override fun setUser(user: User) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(
                localFollowsChannel = localFollowsChannel,
                userId = userId,
                userLogin = userLogin,
                userName = userName,
                channelLogo = channelLogo,
                repository = repository,
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
    ) {
        if (_userId.value != channelId && channelId != null) {
            _userId.value = channelId
            _userLogin.value = channelLogin
            _userName.value = channelName
            _profileImageURL.value = profileImageURL

            viewModelScope.launch {
                try {
                    val stream = repository.loadStreamWithUser(channelId = channelId)
                    val user = stream?.channelUser

                    _stream.postValue(stream)

                    if (user != null) {
                        _loadedUser.postValue(user)
                    } else {
                        loadUser(channelId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        loadUser(channelId = channelId)
    }

    private fun loadUser(channelId: String?) {
        if (channelId != null) {
            viewModelScope.launch {
                try {
                    val user = repository.loadUsersById(
                        ids = mutableListOf(channelId)
                    )?.firstOrNull()
                    _loadedUser.postValue(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun retry() {
        if (_stream.value == null) {
            loadStream(
                channelId = _userId.value,
                channelLogin = _userLogin.value,
                channelName = _userName.value,
                profileImageURL = _profileImageURL.value
            )
        } else {
            if (_stream.value!!.channelUser == null) {
                loadUser(_userId.value)
            }
        }
    }

    fun updateLocalUser(
        context: Context,
        user: com.github.andreyasadchy.xtra.model.helix.user.User,
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
