package com.github.andreyasadchy.xtra.ui.follow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_media.appBar
import kotlinx.android.synthetic.main.fragment_media.tabLayoutMedia
import kotlinx.android.synthetic.main.fragment_media.toolbar
import kotlinx.android.synthetic.main.fragment_media.viewPagerMedia
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class FollowMediaFragment : Fragment(), Injectable, Scrollable {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    companion object {
        private const val LOGGED_IN = "logged_in"

        fun newInstance(loggedIn: Boolean) =
            FollowMediaFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(LOGGED_IN, loggedIn)
                }
            }
    }

    private var previousItem = -1

    private var adapter: Adapter<*>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()

        with(toolbar) {
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.search -> {
                        (activity as? MainActivity)?.openSearch()
                        true
                    }
                    R.id.settings -> {
                        activity?.startActivityFromFragment(
                            this@FollowMediaFragment,
                            Intent(activity, SettingsActivity::class.java),
                            3
                        )
                        true
                    }
                    R.id.login -> {
                        lifecycleScope.launch {
                            onLogout(
                                user = userPreferencesRepository.user.first()
                            )
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { appBar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())

            appBar.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )

            windowInsets
        }

        savedInstanceState
            ?.getInt("previousItem")
            ?.let { previousItem ->
                viewPagerMedia.currentItem = previousItem
            }
    }

    private fun onLogout(user: User) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.logout_title))
            user.login?.let { user ->
                setMessage(
                    getString(
                        R.string.logout_msg,
                        user
                    )
                )
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                activity?.startActivityForResult(
                    Intent(activity, LoginActivity::class.java),
                    2
                )
            }
        }.show()
    }

    private fun onLogin() {
        activity?.startActivityForResult(
            Intent(
                activity,
                LoginActivity::class.java
            ),
            1
        )
    }

    private fun setAdapter() {
        adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> FollowedStreamsFragment()
                    else -> FollowedChannelsFragment()
                }
            }
        }

        viewPagerMedia.adapter = this.adapter

        tabLayoutMedia.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                scrollToTop()
            }
        })

        TabLayoutMediator(tabLayoutMedia, viewPagerMedia) { tab, position ->
            tab.text = context?.getString(
                when (position) {
                    0 -> R.string.live
                    else -> R.string.channels
                }
            )
        }.attach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("previousItem", previousItem)
        super.onSaveInstanceState(outState)
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        (viewPagerMedia.findCurrentFragment() as? Scrollable)?.scrollToTop()
    }

    private fun ViewPager2.findCurrentFragment(): Fragment? {
        return childFragmentManager.findFragmentByTag("f$currentItem")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }
}
