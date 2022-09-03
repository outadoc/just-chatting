package fr.outadoc.justchatting.ui.follow

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
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentMediaBinding
import fr.outadoc.justchatting.model.User
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.follow.channels.FollowedChannelsFragment
import fr.outadoc.justchatting.ui.login.LoginActivity
import fr.outadoc.justchatting.ui.streams.followed.FollowedStreamsFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FollowMediaFragment : Fragment(), Scrollable {

    private val userPreferencesRepository: UserPreferencesRepository by inject()

    companion object {
        fun newInstance() = FollowMediaFragment()
    }

    private var previousItem = -1

    private var viewHolder: FragmentMediaBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = FragmentMediaBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewHolder?.apply {
            setAdapter()
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.search -> {
                        (activity as? NavigationHandler)?.openSearch()
                        true
                    }
                    R.id.settings -> {
                        (activity as? NavigationHandler)?.openSettings()
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

    private fun FragmentMediaBinding.setAdapter() {
        viewPagerMedia.adapter = object : FragmentStateAdapter(this@FollowMediaFragment) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> FollowedStreamsFragment()
                    else -> FollowedChannelsFragment()
                }
            }
        }

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
        viewHolder?.apply {
            appBar.setExpanded(true, true)
            (viewPagerMedia.findCurrentFragment() as? Scrollable)?.scrollToTop()
        }
    }

    private fun ViewPager2.findCurrentFragment(): Fragment? {
        return childFragmentManager.findFragmentByTag("f$currentItem")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
    }
}
