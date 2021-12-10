package com.github.andreyasadchy.xtra.ui.videos.channel

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosAdapter
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.game.GameVideosSortDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class ChannelVideosFragment : BaseVideosFragment<ChannelVideosViewModel>(), GameVideosSortDialog.OnFilter {

    override val viewModel by viewModels<ChannelVideosViewModel> { viewModelFactory }
    override val adapter: BaseVideosAdapter by lazy {
        ChannelVideosAdapter(this, requireActivity() as MainActivity) {
            lastSelectedItem = it
            showDownloadDialog()
        }
    }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.setChannelId(true, requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), requireArguments().getString(C.CHANNEL_ID) ?: "", requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.setChannelId(false, requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), requireArguments().getString(C.CHANNEL_LOGIN) ?: "")
        }
        sortBar.setOnClickListener { GameVideosSortDialog.newInstance(viewModel.sort, viewModel.period, viewModel.type).show(childFragmentManager, null) }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence, type: BroadcastType) {
        adapter.submitList(null)
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.filter(true, requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), sort, period, type, getString(R.string.sort_and_period, sortText, periodText), requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.filter(false, requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), sort, period, type, getString(R.string.sort_and_period, sortText, periodText))
        }
    }
}