package com.github.andreyasadchy.xtra.ui.videos.followed

import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs

class FollowedVideosFragment : BaseVideosFragment<FollowedVideosViewModel>() {

    override val viewModel by viewModels<FollowedVideosViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.setUser(
            user = User.get(requireContext()),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
            apiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_FOLLOWED_VIDEOS, ""), TwitchApiHelper.followedVideosApiDefaults)
        )
    }
}
