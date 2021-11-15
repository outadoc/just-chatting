package com.github.andreyasadchy.xtra.ui.clips.common

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
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
        if (arguments?.getString(C.CHANNEL) != null) {
            ChannelClipsAdapter(this, activity, showDialog)
        } else {
            ClipsAdapter(this, activity, activity, showDialog)
        }
    }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        viewModel.loadClips(requireContext().prefs().getString(C.CLIENT_ID, ""), requireContext().prefs().getString(C.TOKEN, ""), arguments?.getString(C.CHANNEL), arguments?.getString(C.GAME))
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        adapter.submitList(null)
        viewModel.filter(index, text)
    }
}
