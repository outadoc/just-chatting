package com.github.andreyasadchy.xtra.ui.streams.common

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.ui.streams.StreamsCompactAdapter
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.fragment_streams.*

class StreamsFragment : BaseStreamsFragment<StreamsViewModel>(), FollowFragment {

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
        viewModel.loadStreams(
            gameId = arguments?.getString(C.GAME_ID),
            gameName = arguments?.getString(C.GAME_NAME),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = requireContext().prefs().getString(C.TOKEN, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
            tags = arguments?.getStringArray(C.TAGS)?.toList(),
            apiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_STREAMS, ""), TwitchApiHelper.streamsApiDefaults),
            gameApiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_GAME_STREAMS, ""), TwitchApiHelper.gameStreamsApiDefaults),
            thumbnailsEnabled = !compactStreams
        )
        val activity = requireActivity() as MainActivity
        sortBar.visible()
        if (arguments?.getString(C.GAME_ID) != null && arguments?.getString(C.GAME_NAME) != null) {
            sortBar.setOnClickListener { activity.openTagSearch(gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME)) }
            if ((requireContext().prefs().getString(C.UI_FOLLOW_BUTTON, "0")?.toInt() ?: 0) < 2) {
                parentFragment?.followGame?.let { initializeFollow(
                    fragment = this,
                    viewModel = viewModel,
                    followButton = it,
                    setting = requireContext().prefs().getString(C.UI_FOLLOW_BUTTON, "0")?.toInt() ?: 0,
                    user = User.get(activity),
                    helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
                    gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
                ) }
            }
            if (arguments?.getBoolean(C.CHANNEL_UPDATELOCAL) == true) {
                viewModel.updateLocalGame(requireContext())
            }
        } else {
            sortBar.setOnClickListener { activity.openTagSearch() }
        }
    }
}