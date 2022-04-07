package com.github.andreyasadchy.xtra.ui.downloads

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.ui.videos.offline.BaseOfflineVideosAdapter
import com.github.andreyasadchy.xtra.ui.videos.offline.BaseOfflineVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.offline.BaseOfflineViewModel
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_downloads.*

class DownloadsFragment : BaseOfflineVideosFragment<BaseOfflineViewModel>() {

    interface OnVideoSelectedListener {
        fun startOfflineVideo(video: OfflineVideo)
    }

    override val viewModel by viewModels<DownloadsViewModel> { viewModelFactory }
    override val adapter: BaseOfflineVideosAdapter by lazy {
        DownloadsAdapter(this, requireActivity() as MainActivity, requireActivity() as MainActivity, requireActivity() as MainActivity, requireActivity() as MainActivity, {
            val delete = getString(R.string.delete)
            AlertDialog.Builder(activity)
                .setTitle(delete)
                .setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(delete) { _, _ -> viewModel.deleteDownload(requireContext(), it) }
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show()
        }, {
            viewModel.vodIgnoreUser(it)
        })
    }

    override fun initialize() {
        super.initialize()
        val activity = requireActivity() as MainActivity
        val isLoggedIn = User.get(activity) !is NotLoggedIn
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        viewModel.offlineVideos.observe(viewLifecycleOwner) {
            viewModel.loadVideos(
                helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
                helixToken = requireContext().prefs().getString(C.TOKEN, ""),
                gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
                vodTimeLeft = requireContext().prefs().getBoolean(C.UI_BOOKMARK_TIME_LEFT, true),
                currentList = adapter.currentList
            )
        }
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                adapter.unregisterAdapterDataObserver(this)
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        if (positionStart == 0) {
                            appBar?.setExpanded(true, true)
                            recyclerView.smoothScrollToPosition(0)
                        }
                    }
                })
            }
        })
        search.setOnClickListener { activity.openSearch() }
        menu.setOnClickListener { it ->
            PopupMenu(activity, it).apply {
                inflate(R.menu.top_menu)
                menu.findItem(R.id.login).title = if (isLoggedIn) getString(R.string.log_out) else getString(R.string.log_in)
                setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.settings -> { activity.startActivityFromFragment(this@DownloadsFragment, Intent(activity, SettingsActivity::class.java), 3) }
                        R.id.login -> {
                            if (!isLoggedIn) {
                                activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1)
                            } else {
                                androidx.appcompat.app.AlertDialog.Builder(activity)
                                    .setTitle(getString(R.string.logout_title))
                                    .setMessage(getString(R.string.logout_msg, context?.prefs()?.getString(C.USERNAME, "")))
                                    .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                        activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 2) }
                                    .show()
                            }
                        }
                        else -> menu.close()
                    }
                    true
                }
                show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }
}
