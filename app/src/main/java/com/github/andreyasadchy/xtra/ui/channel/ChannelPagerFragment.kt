package com.github.andreyasadchy.xtra.ui.channel

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.Utils
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.util.*
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.fragment_media_pager.*


class ChannelPagerFragment : MediaPagerFragment(), FollowFragment, Scrollable {

    companion object {
        fun newInstance(id: String?, login: String?, name: String?, channelLogo: String?, updateLocal: Boolean = false) = ChannelPagerFragment().apply {
            arguments = Bundle().apply {
                putString(C.CHANNEL_ID, id)
                putString(C.CHANNEL_LOGIN, login)
                putString(C.CHANNEL_DISPLAYNAME, name)
                putString(C.CHANNEL_PROFILEIMAGE, channelLogo)
                putBoolean(C.CHANNEL_UPDATELOCAL, updateLocal)
            }
        }
    }

    private val viewModel by viewModels<ChannelPagerViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val isLoggedIn = User.get(activity) !is NotLoggedIn
        setAdapter(ChannelPagerAdapter(activity, childFragmentManager, requireArguments()))
        if (activity.isInLandscapeOrientation) {
            appBar.setExpanded(false, false)
        }
        requireArguments().getString(C.CHANNEL_DISPLAYNAME).let {
            if (it != null) {
                userLayout.visible()
                userName.visible()
                userName.text = it
            } else {
                userName.gone()
            }
        }
        requireArguments().getString(C.CHANNEL_PROFILEIMAGE).let {
            if (it != null) {
                userLayout.visible()
                userImage.visible()
                userImage.loadImage(this, it, circle = true)
            } else {
                userImage.gone()
            }
        }
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
        search.setOnClickListener { activity.openSearch() }
        menu.setOnClickListener { it ->
            PopupMenu(activity, it).apply {
                inflate(R.menu.top_menu)
                menu.findItem(R.id.login).title = if (isLoggedIn) getString(R.string.log_out) else getString(R.string.log_in)
                setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.settings -> { activity.startActivityFromFragment(this@ChannelPagerFragment, Intent(activity, SettingsActivity::class.java), 3) }
                        R.id.login -> {
                            if (!isLoggedIn) {
                                activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1)
                            } else {
                                AlertDialog.Builder(activity)
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
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            private val layoutParams = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
            private val originalScrollFlags = layoutParams.scrollFlags

            override fun onPageSelected(position: Int) {
//                layoutParams.scrollFlags = if (position != 3) {
                layoutParams.scrollFlags = if (position != 2) {
                    originalScrollFlags
                } else {
                    appBar.setExpanded(false, isResumed)
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })
    }

    override fun initialize() {
        val activity = requireActivity() as MainActivity
        viewModel.loadStream(channelId = requireArguments().getString(C.CHANNEL_ID), channelLogin = requireArguments().getString(C.CHANNEL_LOGIN), channelName = requireArguments().getString(C.CHANNEL_DISPLAYNAME), profileImageURL = requireArguments().getString(C.CHANNEL_PROFILEIMAGE), helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), helixToken = requireContext().prefs().getString(C.TOKEN, ""), gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""))
        viewModel.stream.observe(viewLifecycleOwner) { stream ->
            updateStreamLayout(stream)
            if (stream?.channelUser != null) {
                updateUserLayout(stream.channelUser)
            } else {
                viewModel.loadUser(channelId = requireArguments().getString(C.CHANNEL_ID), helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), helixToken = requireContext().prefs().getString(C.TOKEN, ""), gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""))
                viewModel.user.observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        updateUserLayout(user)
                    }
                }
            }
        }
        if ((requireContext().prefs().getString(C.UI_FOLLOW_BUTTON, "0")?.toInt() ?: 0) < 2) {
            initializeFollow(
                fragment = this,
                viewModel = viewModel,
                followButton = follow,
                setting = requireContext().prefs().getString(C.UI_FOLLOW_BUTTON, "0")?.toInt() ?: 0,
                user = User.get(activity),
                helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
                gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
            )
        }
    }

    private fun updateStreamLayout(stream: Stream?) {
        val activity = requireActivity() as MainActivity
        if (stream?.type?.lowercase() == "rerun") {
            watchLive.text = getString(R.string.watch_rerun)
            watchLive.setOnClickListener { activity.startStream(stream) }
        } else {
            if (stream?.viewer_count != null) {
                watchLive.text = getString(R.string.watch_live)
                watchLive.setOnClickListener { activity.startStream(stream) }
            } else {
                watchLive.setOnClickListener { activity.startStream(Stream(user_id = requireArguments().getString(C.CHANNEL_ID), user_login = requireArguments().getString(C.CHANNEL_LOGIN), user_name = requireArguments().getString(C.CHANNEL_DISPLAYNAME), profileImageURL = requireArguments().getString(C.CHANNEL_PROFILEIMAGE))) }
                if (stream?.lastBroadcast != null) {
                    TwitchApiHelper.formatTimeString(requireContext(), stream.lastBroadcast).let {
                        if (it != null)  {
                            lastBroadcast.visible()
                            lastBroadcast.text = requireContext().getString(R.string.last_broadcast_date, it)
                        } else {
                            lastBroadcast.gone()
                        }
                    }
                }
            }
        }
        stream?.channelLogo.let {
            if (it != null) {
                userLayout.visible()
                userImage.visible()
                userImage.loadImage(this, it, circle = true)
                requireArguments().putString(C.CHANNEL_PROFILEIMAGE, it)
            } else {
                userImage.gone()
            }
        }
        stream?.user_name.let {
            if (it != null && it != requireArguments().getString(C.CHANNEL_DISPLAYNAME)) {
                userLayout.visible()
                userName.visible()
                userName.text = it
                requireArguments().putString(C.CHANNEL_DISPLAYNAME, it)
            }
        }
        stream?.user_login.let {
            if (it != null && it != requireArguments().getString(C.CHANNEL_LOGIN)) {
                requireArguments().putString(C.CHANNEL_LOGIN, it)
            }
        }
        if (stream?.title != null) {
            streamLayout.visible()
            title.visible()
            title.text = stream.title.trim()
        } else {
            title.gone()
        }
        if (stream?.game_name != null) {
            streamLayout.visible()
            gameName.visible()
            gameName.text = stream.game_name
            if (stream.game_id != null) {
                gameName.setOnClickListener { activity.openGame(stream.game_id, stream.game_name) }
            }
        } else {
            gameName.gone()
        }
        if (stream?.viewer_count != null) {
            streamLayout.visible()
            viewers.visible()
            viewers.text = TwitchApiHelper.formatViewersCount(requireContext(), stream.viewer_count)
        } else {
            viewers.gone()
        }
        if (requireContext().prefs().getBoolean(C.UI_UPTIME, true)) {
            if (stream?.started_at != null) {
                TwitchApiHelper.getUptime(requireContext(), stream.started_at).let {
                    if (it != null)  {
                        streamLayout.visible()
                        uptime.visible()
                        uptime.text = requireContext().getString(R.string.uptime, it)
                    } else {
                        uptime.gone()
                    }
                }
            }
        }
    }

    private fun updateUserLayout(user: com.github.andreyasadchy.xtra.model.helix.user.User) {
        if (!userImage.isVisible && user.channelLogo != null) {
            userLayout.visible()
            userImage.visible()
            userImage.loadImage(this, user.channelLogo, circle = true)
            requireArguments().putString(C.CHANNEL_PROFILEIMAGE, user.channelLogo)
        }
        if (user.bannerImageURL != null) {
            bannerImage.visible()
            bannerImage.loadImage(this, user.bannerImageURL)
            if (userName.isVisible) {
                userName.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            bannerImage.gone()
        }
        if (user.created_at != null) {
            userCreated.visible()
            userCreated.text = requireContext().getString(R.string.created_at, TwitchApiHelper.formatTimeString(requireContext(), user.created_at))
            if (user.bannerImageURL != null) {
                userCreated.setTextColor(Color.LTGRAY)
                userCreated.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            userCreated.gone()
        }
        if (user.followers_count != null) {
            userFollowers.visible()
            userFollowers.text = requireContext().getString(R.string.followers, TwitchApiHelper.formatCount(requireContext(), user.followers_count))
            if (user.bannerImageURL != null) {
                userFollowers.setTextColor(Color.LTGRAY)
                userFollowers.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            userFollowers.gone()
        }
        if (user.view_count != null) {
            userViews.visible()
            userViews.text = TwitchApiHelper.formatViewsCount(requireContext(), user.view_count)
            if (user.bannerImageURL != null) {
                userViews.setTextColor(Color.LTGRAY)
                userViews.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            userViews.gone()
        }
        val broadcasterType = if (user.broadcaster_type != null) { TwitchApiHelper.getUserType(requireContext(), user.broadcaster_type) } else null
        val type = if (user.type != null) { TwitchApiHelper.getUserType(requireContext(), user.type) } else null
        val typeString = if (broadcasterType != null && type != null) "$broadcasterType, $type" else broadcasterType ?: type
        if (typeString != null) {
            userType.visible()
            userType.text = typeString
            if (user.bannerImageURL != null) {
                userType.setTextColor(Color.LTGRAY)
                userType.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            userType.gone()
        }
        if (requireArguments().getBoolean(C.CHANNEL_UPDATELOCAL)) {
            viewModel.updateLocalUser(requireContext(), user)
        }
    }

    override fun onNetworkRestored() {
        viewModel.retry(helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), helixToken = requireContext().prefs().getString(C.TOKEN, ""), gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            appBar.setExpanded(false, false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        (currentFragment as? Scrollable)?.scrollToTop()
    }
}