package com.github.andreyasadchy.xtra.ui.streams.followed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override val viewModel by viewModels<FollowedStreamsViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadStreams()
    }
}
