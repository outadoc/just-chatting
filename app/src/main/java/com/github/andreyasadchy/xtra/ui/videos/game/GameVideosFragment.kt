package com.github.andreyasadchy.xtra.ui.videos.game

import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.VideosSortDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class GameVideosFragment : BaseVideosFragment<GameVideosViewModel>(), VideosSortDialog.OnFilter, FollowFragment {

    override val viewModel by viewModels<GameVideosViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner) {
            sortText.text = it
        }
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.setGame(useHelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME), token = requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.setGame(useHelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME))
        }
        sortBar.setOnClickListener { VideosSortDialog.newInstance(sort = viewModel.sort, period = viewModel.period, type = viewModel.type, languageIndex = viewModel.languageIndex).show(childFragmentManager, null) }
        val activity = requireActivity() as MainActivity
        if (requireContext().prefs().getBoolean(C.UI_FOLLOW, true)) {
            parentFragment?.followGame?.let { initializeFollow(this, viewModel, it, User.get(activity), requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), requireContext().prefs().getString(C.GQL_CLIENT_ID, "")) }
        }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence, type: BroadcastType, languageIndex: Int) {
        adapter.submitList(null)
        if (period != Period.WEEK || requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.filter(useHelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), sort = sort, period = period, type = type, languageIndex = languageIndex, text = getString(R.string.sort_and_period, sortText, periodText), token = requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.filter(useHelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), sort = sort, period = period, type = type, languageIndex = languageIndex, text = getString(R.string.sort_and_period, sortText, periodText))
        }
    }
}
