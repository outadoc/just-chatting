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
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_media.appBar
import kotlinx.android.synthetic.main.fragment_media.toolbar

class FollowMediaFragment : Fragment(), Scrollable {

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
    private var currentFragment: Fragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = User.get(requireContext())

        toolbar.inflateMenu(R.menu.top_menu)

        toolbar.menu.findItem(R.id.login).title =
            if (user !is NotLoggedIn) getString(R.string.log_out)
            else getString(R.string.log_in)

        toolbar.setOnMenuItemClickListener { menuItem ->
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

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { appBar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())

            appBar.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )

            WindowInsetsCompat.CONSUMED
        }

        currentFragment = if (previousItem != -2) {
            val loggedIn = requireArguments().getBoolean(LOGGED_IN)
            val newFragment = FollowPagerFragment.newInstance(loggedIn)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .commit()

            previousItem = -2
            newFragment
        } else {
            childFragmentManager.findFragmentById(R.id.fragmentContainer)
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("previousItem", previousItem)
        super.onSaveInstanceState(outState)
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        (currentFragment as? Scrollable)?.scrollToTop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }
}
