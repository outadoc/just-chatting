package com.github.andreyasadchy.xtra.ui.follow.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_followed_channels.*

class FollowedGamesFragment : PagedListFragment<Game, FollowedGamesViewModel, BasePagedListAdapter<Game>>(), Scrollable {

    override val viewModel by viewModels<FollowedGamesViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Game> by lazy {
        val activity = requireActivity() as MainActivity
        FollowedGamesAdapter(this, activity, activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_followed_channels, container, false)
    }

    override fun initialize() {
        super.initialize()
        sortBar.gone()
        viewModel.setUser(
            user = User.get(requireContext()),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
            apiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_FOLLOWED_GAMES, ""), TwitchApiHelper.followedGamesApiDefaults)
        )
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}