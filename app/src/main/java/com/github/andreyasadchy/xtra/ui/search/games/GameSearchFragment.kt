package com.github.andreyasadchy.xtra.ui.search.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.search.Searchable
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

class GameSearchFragment : PagedListFragment<Game, GameSearchViewModel, BasePagedListAdapter<Game>>(), Searchable {

    override val viewModel by viewModels<GameSearchViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Game> by lazy { GameSearchAdapter(this, requireActivity() as MainActivity, requireActivity() as MainActivity) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.common_recycler_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh.isEnabled = false
    }

    override fun search(query: String) {
        if (query.isNotEmpty()) {
            if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
                viewModel.setQuery(true, requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), query, requireContext().prefs().getString(C.TOKEN, ""))
            } else {
                viewModel.setQuery(false, requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), query)
            }
        } else {
            adapter.submitList(null)
            nothingHere?.gone()
        }
    }
}