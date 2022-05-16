package com.github.andreyasadchy.xtra.ui.common.follow

import com.github.andreyasadchy.xtra.model.User

interface FollowViewModel {
    val userId: String?
    val userLogin: String?
    val userName: String?
    val channelLogo: String?
    val follow: FollowLiveData
    val game: Boolean
        get() = false

    fun setUser(user: User, helixClientId: String?, gqlClientId: String?, setting: Int)
}