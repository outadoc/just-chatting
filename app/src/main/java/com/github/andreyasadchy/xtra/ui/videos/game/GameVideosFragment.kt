package com.github.andreyasadchy.xtra.ui.videos.game

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class GameVideosFragment : BaseVideosFragment<GameVideosViewModel>(), GameVideosSortDialog.OnFilter {

    override val viewModel by viewModels<GameVideosViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.setGame(true, requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), requireArguments().getString(C.GAME_ID)!!, requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.setGame(false, requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), requireArguments().getString(C.GAME_NAME)!!)
        }
        sortBar.setOnClickListener { GameVideosSortDialog.newInstance(viewModel.sort, viewModel.period).show(childFragmentManager, null) }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        adapter.submitList(null)
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.filter(true, requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), sort, period, getString(R.string.sort_and_period, sortText, periodText), requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.filter(false, requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), sort, period, getString(R.string.sort_and_period, sortText, periodText))
        }
    }
}
