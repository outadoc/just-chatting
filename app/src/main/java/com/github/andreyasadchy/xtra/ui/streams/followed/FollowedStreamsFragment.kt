package com.github.andreyasadchy.xtra.ui.streams.followed

import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override val viewModel by viewModels<FollowedStreamsViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.loadStreams(
            userId = User.get(requireContext()).id,
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = User.get(requireContext()).helixToken
        )
    }
}
