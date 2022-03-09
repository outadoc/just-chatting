package com.github.andreyasadchy.xtra.ui.player.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.view.GridRecyclerView
import com.github.andreyasadchy.xtra.util.C

class PlayerGamesFragment : PagedListFragment<Game, PlayerGamesViewModel, BasePagedListAdapter<Game>>() {

    override val viewModel by viewModels<PlayerGamesViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Game> by lazy { PlayerGamesAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val recycleView = GridRecyclerView(context).apply {
            id = R.id.recyclerView
            setLayoutParams(layoutParams)
        }
        return recycleView
    }

    override fun initialize() {
        super.initialize()
        viewModel.loadGames(arguments?.getParcelableArrayList<Game>(C.GAMES_LIST)?.toList())
    }
}