package com.github.andreyasadchy.xtra.ui.clips.common

import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.clips.ClipsAdapter
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.videos.VideosSortDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_clips.*
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.sort_bar.*

class ClipsFragment : BaseClipsFragment<ClipsViewModel>(), VideosSortDialog.OnFilter, FollowFragment {

    override val viewModel by viewModels<ClipsViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Clip> by lazy {
        val activity = requireActivity() as MainActivity
        val showDialog: (Clip) -> Unit = {
            lastSelectedItem = it
            showDownloadDialog()
        }
        if (arguments?.getString(C.CHANNEL_ID) != null) {
            ChannelClipsAdapter(this, activity, activity, showDialog)
        } else {
            ClipsAdapter(this, activity, activity, activity, showDialog)
        }
    }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner) {
            sortText.text = it
        }
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.loadClips(useHelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), channelId = arguments?.getString(C.CHANNEL_ID), channelLogin = arguments?.getString(C.CHANNEL_LOGIN), gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME), token = requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.loadClips(useHelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), channelId = arguments?.getString(C.CHANNEL_ID), gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME))
        }
        sortBar.setOnClickListener { VideosSortDialog.newInstance(period = viewModel.period, languageIndex = viewModel.languageIndex, clipChannel = arguments?.getString(C.CHANNEL_ID) != null).show(childFragmentManager, null) }
        val activity = requireActivity() as MainActivity
        if (adapter is ClipsAdapter && requireContext().prefs().getBoolean(C.UI_FOLLOW, true)) {
            parentFragment?.followGame?.let { initializeFollow(this, viewModel, it, User.get(activity), requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), requireContext().prefs().getString(C.GQL_CLIENT_ID, "")) }
        }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence, type: BroadcastType, languageIndex: Int) {
        adapter.submitList(null)
        if (languageIndex == 0 && requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.filter(useHelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), period = period, languageIndex = languageIndex, text = getString(R.string.sort_and_period, sortText, periodText), token = requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.filter(useHelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), period = period, languageIndex = languageIndex, text = getString(R.string.sort_and_period, sortText, periodText))
        }
    }
}
