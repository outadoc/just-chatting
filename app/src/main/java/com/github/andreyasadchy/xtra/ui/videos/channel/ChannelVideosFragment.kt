package com.github.andreyasadchy.xtra.ui.videos.channel

import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosAdapter
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.VideosSortDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class ChannelVideosFragment : BaseVideosFragment<ChannelVideosViewModel>(), VideosSortDialog.OnFilter {

    override val viewModel by viewModels<ChannelVideosViewModel> { viewModelFactory }
    override val adapter: BaseVideosAdapter by lazy {
        ChannelVideosAdapter(this, requireActivity() as MainActivity, requireActivity() as MainActivity) {
            lastSelectedItem = it
            showDownloadDialog()
        }
    }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner) {
            sortText.text = it
        }
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.setChannelId(useHelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), channelId = requireArguments().getString(C.CHANNEL_ID) ?: "", token = requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.setChannelId(useHelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), channelId = requireArguments().getString(C.CHANNEL_ID) ?: "")
        }
        sortBar.setOnClickListener { VideosSortDialog.newInstance(sort = viewModel.sort, period = viewModel.period, type = viewModel.type).show(childFragmentManager, null) }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence, type: BroadcastType, languageIndex: Int) {
        adapter.submitList(null)
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.filter(useHelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), sort = sort, period = period, type = type, text = getString(R.string.sort_and_period, sortText, periodText), token = requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.filter(useHelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), sort = sort, period = period, type = type, text = getString(R.string.sort_and_period, sortText, periodText))
        }
    }
}