package com.github.andreyasadchy.xtra.ui.common

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
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_media.appBar
import kotlinx.android.synthetic.main.fragment_media.toolbar

abstract class MediaFragment : Fragment(), Scrollable {

    var previousItem = -1
    var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousItem = savedInstanceState?.getInt("previousItem", -1) ?: -1
    }

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
                        this@MediaFragment,
                        Intent(activity, SettingsActivity::class.java),
                        3
                    )
                    true
                }
                R.id.login -> {
                    if (user is NotLoggedIn) {
                        activity?.startActivityForResult(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            ),
                            1
                        )
                    } else {
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("previousItem", previousItem)
        super.onSaveInstanceState(outState)
    }

    abstract fun onSpinnerItemSelected(position: Int): Fragment

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
