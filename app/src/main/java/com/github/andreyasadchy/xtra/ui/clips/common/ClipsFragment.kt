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
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
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
        if (arguments?.getString(C.CHANNEL_ID) != null || arguments?.getString(C.CHANNEL_LOGIN) != null) {
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
        viewModel.loadClips(
            context = requireContext(),
            channelId = arguments?.getString(C.CHANNEL_ID),
            channelLogin = arguments?.getString(C.CHANNEL_LOGIN),
            gameId = arguments?.getString(C.GAME_ID),
            gameName = arguments?.getString(C.GAME_NAME),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = requireContext().prefs().getString(C.TOKEN, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
            channelApiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_CHANNEL_CLIPS, ""), TwitchApiHelper.channelClipsApiDefaults),
            gameApiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_GAME_CLIPS, ""), TwitchApiHelper.gameClipsApiDefaults)
        )
        sortBar.setOnClickListener { VideosSortDialog.newInstance(period = viewModel.period, languageIndex = viewModel.languageIndex, clipChannel = arguments?.getString(C.CHANNEL_ID) != null, saveSort = viewModel.saveSort).show(childFragmentManager, null) }
        val activity = requireActivity() as MainActivity
        if (adapter is ClipsAdapter && (requireContext().prefs().getString(C.UI_FOLLOW_BUTTON, "0")?.toInt() ?: 0) < 2) {
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
            period = period,
            languageIndex = languageIndex,
            text = getString(R.string.sort_and_period, sortText, periodText),
            saveSort = saveSort
        )
    }
}
