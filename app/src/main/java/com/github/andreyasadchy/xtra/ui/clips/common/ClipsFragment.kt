package com.github.andreyasadchy.xtra.ui.clips.common

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.clips.ClipsAdapter
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.FragmentUtils
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_clips.*
import kotlinx.android.synthetic.main.sort_bar.*

class ClipsFragment : BaseClipsFragment<ClipsViewModel>() {

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
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.loadClips(usehelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), channelId = arguments?.getString(C.CHANNEL_ID), channelLogin = arguments?.getString(C.CHANNEL_LOGIN), gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME), token = requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.loadClips(usehelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), channelId = arguments?.getString(C.CHANNEL_ID), gameId = arguments?.getString(C.GAME_ID), gameName = arguments?.getString(C.GAME_NAME))
        }
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        val period: Period = when (tag) {
            R.string.today -> Period.DAY
            R.string.this_week -> Period.WEEK
            R.string.this_month -> Period.MONTH
            else -> Period.ALL
        }
        adapter.submitList(null)
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.filter(true, requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), period, index, text, requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.filter(false, requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), period, index, text)
        }
    }
}
