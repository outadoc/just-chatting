package com.github.andreyasadchy.xtra.ui.streams.followed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import javax.inject.Inject

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override val viewModel by viewModels<FollowedStreamsViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadStreams(
            userId = User.get(requireContext()).id,
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = User.get(requireContext()).helixToken
        )
    }
}
