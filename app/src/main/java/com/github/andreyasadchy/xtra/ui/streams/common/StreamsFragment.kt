package com.github.andreyasadchy.xtra.ui.streams.common

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.ui.streams.StreamsCompactAdapter
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs

class StreamsFragment : BaseStreamsFragment<StreamsViewModel>() {

    override val viewModel by viewModels<StreamsViewModel> { viewModelFactory }

    override val adapter: BasePagedListAdapter<Stream> by lazy {
        if (!compactStreams) {
            super.adapter
        } else {
            val activity = requireActivity() as MainActivity
            StreamsCompactAdapter(this, activity, activity, activity)
        }
    }

    private var compactStreams = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compactStreams = requireContext().prefs().getBoolean(C.COMPACT_STREAMS, false)
    }

    override fun initialize() {
        super.initialize()
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.loadStreams(usehelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), token = requireContext().prefs().getString(C.TOKEN, ""), gameId = arguments?.getString(C.GAME_ID))
        } else {
            viewModel.loadStreams(usehelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME))}
    }
}