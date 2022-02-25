package com.github.andreyasadchy.xtra.ui.downloads

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_downloads.*
import javax.inject.Inject

class DownloadsFragment : Fragment(), Injectable, Scrollable {

    interface OnVideoSelectedListener {
        fun startOfflineVideo(video: OfflineVideo)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<DownloadsViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = requireActivity() as MainActivity
        val isLoggedIn = User.get(activity) !is NotLoggedIn
        val adapter = DownloadsAdapter(this, activity, activity, activity) {
            val delete = getString(R.string.delete)
            AlertDialog.Builder(activity)
                .setTitle(delete)
                .setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(delete) { _, _ -> viewModel.delete(requireContext(), it) }
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show()
        }
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        viewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            text.isVisible = it.isEmpty()
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
