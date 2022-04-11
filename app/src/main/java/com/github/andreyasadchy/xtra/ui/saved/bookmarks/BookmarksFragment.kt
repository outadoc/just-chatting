package com.github.andreyasadchy.xtra.ui.saved.bookmarks

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.download.VideoDownloadDialog
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_saved.*
import javax.inject.Inject

class BookmarksFragment : Fragment(), Injectable, Scrollable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<BookmarksViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = requireActivity() as MainActivity
        val adapter = BookmarksAdapter(this, activity, activity, activity, {
            VideoDownloadDialog.newInstance(it).show(childFragmentManager, null)
        }, {
            viewModel.vodIgnoreUser(it)
        }, {
            val delete = getString(R.string.delete)
            AlertDialog.Builder(activity)
                .setTitle(delete)
                .setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(delete) { _, _ -> viewModel.delete(requireContext(), it) }
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show()
        })
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        viewModel.bookmarks.observe(viewLifecycleOwner) {
            adapter.submitList(it.reversed())
            nothingHere?.isVisible = it.isEmpty()
            if (requireContext().prefs().getBoolean(C.UI_BOOKMARK_TIME_LEFT, true)) {
                viewModel.loadUsers(
                    helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
                    helixToken = requireContext().prefs().getString(C.TOKEN, ""),
                    gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
                )
            }
            if (!requireContext().prefs().getString(C.TOKEN, "").isNullOrEmpty()) {
                viewModel.loadVideos(
                    helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
                    helixToken = requireContext().prefs().getString(C.TOKEN, ""),
                )
            }
        }
        if (requireContext().prefs().getBoolean(C.PLAYER_USE_VIDEOPOSITIONS, true)) {
            viewModel.positions.observe(viewLifecycleOwner) {
                adapter.setVideoPositions(it)
            }
        }
        if (requireContext().prefs().getBoolean(C.UI_BOOKMARK_TIME_LEFT, true)) {
            viewModel.ignoredUsers.observe(viewLifecycleOwner) {
                adapter.setIgnoredUsers(it)
            }
            viewModel.users.observe(viewLifecycleOwner) {
                adapter.setLoadedUsers(it)
            }
        }
        if (!requireContext().prefs().getString(C.TOKEN, "").isNullOrEmpty()) {
            viewModel.videos.observe(viewLifecycleOwner) {
                adapter.setLoadedVideos(it)
            }
        }
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                adapter.unregisterAdapterDataObserver(this)
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        if (positionStart == 0) {
                            recyclerView.smoothScrollToPosition(0)
                        }
                    }
                })
            }
        })
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }
}
