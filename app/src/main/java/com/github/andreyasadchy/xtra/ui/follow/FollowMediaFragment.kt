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
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_media.appBar
import kotlinx.android.synthetic.main.fragment_media.toolbar
import kotlinx.android.synthetic.main.fragment_media.viewPager

class FollowMediaFragment : BaseNetworkFragment(), Scrollable {

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

    private var adapter: ItemAwareFragmentPagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter(FollowPagerAdapter(requireContext(), childFragmentManager))

        with(toolbar) {
            val user = User.get(requireContext())
            inflateMenu(R.menu.top_menu)

            menu.findItem(R.id.login).title =
                if (user !is NotLoggedIn) getString(R.string.log_out)
                else getString(R.string.log_in)

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
                        when (user) {
                            is NotLoggedIn -> onLogin()
                            else -> onLogout(user)
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
                viewPager.currentItem = previousItem
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

    private fun setAdapter(adapter: ItemAwareFragmentPagerAdapter) {
        this.adapter = adapter
        viewPager.adapter = adapter
        viewPager.currentItem = 0
        viewPager.offscreenPageLimit = adapter.count
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("previousItem", previousItem)
        super.onSaveInstanceState(outState)
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        (adapter?.currentFragment as? Scrollable)?.scrollToTop()
    }

    override fun initialize() {
    }

    override fun onNetworkRestored() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }
}
