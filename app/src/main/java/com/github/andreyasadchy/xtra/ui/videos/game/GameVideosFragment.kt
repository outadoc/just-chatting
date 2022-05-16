package com.github.andreyasadchy.xtra.ui.videos.game

import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosAdapter
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.VideosAdapter
import com.github.andreyasadchy.xtra.ui.videos.VideosSortDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class GameVideosFragment : BaseVideosFragment<GameVideosViewModel>(), VideosSortDialog.OnFilter, FollowFragment {

    override val viewModel by viewModels<GameVideosViewModel> { viewModelFactory }

    override val adapter: BaseVideosAdapter by lazy {
        val activity = requireActivity() as MainActivity
        VideosAdapter(this, activity, activity, activity, {
            lastSelectedItem = it
            showDownloadDialog()
        }, {
            lastSelectedItem = it
            viewModel.saveBookmark(requireContext(), it)
        })
    }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner) {
            sortText.text = it
        }
        viewModel.setGame(
            context = requireContext(),
            gameId = arguments?.getString(C.GAME_ID),
            gameName = arguments?.getString(C.GAME_NAME),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = requireContext().prefs().getString(C.TOKEN, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
            apiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_GAME_VIDEOS, ""), TwitchApiHelper.gameVideosApiDefaults)
        )
        sortBar.setOnClickListener { VideosSortDialog.newInstance(sort = viewModel.sort, period = viewModel.period, type = viewModel.type, languageIndex = viewModel.languageIndex, saveSort = viewModel.saveSort).show(childFragmentManager, null) }
        val activity = requireActivity() as MainActivity
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
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence, type: BroadcastType, languageIndex: Int, saveSort: Boolean) {
        adapter.submitList(null)
        viewModel.filter(
            sort = sort,
            period = period,
            type = type,
            languageIndex = languageIndex,
            text = getString(R.string.sort_and_period, sortText, periodText),
            saveSort = saveSort
        )
    }
}
