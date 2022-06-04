package com.github.andreyasadchy.xtra.ui.channel

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.Utils
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_channel.appBar
import kotlinx.android.synthetic.main.fragment_channel.bannerImage
import kotlinx.android.synthetic.main.fragment_channel.follow
import kotlinx.android.synthetic.main.fragment_channel.gameName
import kotlinx.android.synthetic.main.fragment_channel.lastBroadcast
import kotlinx.android.synthetic.main.fragment_channel.spacerTop
import kotlinx.android.synthetic.main.fragment_channel.title
import kotlinx.android.synthetic.main.fragment_channel.toolbar
import kotlinx.android.synthetic.main.fragment_channel.uptime
import kotlinx.android.synthetic.main.fragment_channel.userImage
import kotlinx.android.synthetic.main.fragment_channel.viewers
import kotlinx.android.synthetic.main.fragment_channel.watchLive

class ChannelPagerFragment : MediaPagerFragment(), FollowFragment, Scrollable {

    companion object {
        fun newInstance(
            id: String?,
            login: String?,
            name: String?,
            channelLogo: String?,
            updateLocal: Boolean = false
        ) = ChannelPagerFragment().apply {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val args = requireArguments()

        childFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ChatFragment.newInstance(
                    args.getString(C.CHANNEL_ID),
                    args.getString(C.CHANNEL_LOGIN),
                    args.getString(C.CHANNEL_DISPLAYNAME)
                )
            )
            .commit()

        toolbar.title = args.getString(C.CHANNEL_DISPLAYNAME)

        args.getString(C.CHANNEL_PROFILEIMAGE).let { profileImage ->
            if (profileImage != null) {
                userImage.visible()
                userImage.loadImage(this, profileImage, circle = true)
            } else {
                userImage.gone()
            }
        }

        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }

        watchLive.setOnClickListener {
            TODO("open stream intent")
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            spacerTop.updateLayoutParams<MarginLayoutParams> {
                height = insets.top
            }
            windowInsets
        }
    }

    override fun initialize() {
        val activity = requireActivity() as MainActivity
        viewModel.loadStream(
            channelId = requireArguments().getString(C.CHANNEL_ID),
            channelLogin = requireArguments().getString(C.CHANNEL_LOGIN),
            channelName = requireArguments().getString(C.CHANNEL_DISPLAYNAME),
            profileImageURL = requireArguments().getString(C.CHANNEL_PROFILEIMAGE),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = requireContext().prefs().getString(C.TOKEN, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
        )
        viewModel.stream.observe(viewLifecycleOwner) { stream ->
            updateStreamLayout(stream)
            if (stream?.channelUser != null) {
                updateUserLayout(stream.channelUser)
            } else {
                viewModel.loadUser(
                    channelId = requireArguments().getString(C.CHANNEL_ID),
                    helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
                    helixToken = requireContext().prefs().getString(C.TOKEN, ""),
                    gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
                )
                viewModel.user.observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        updateUserLayout(user)
                    }
                }
            }
        }

        initializeFollow(
            fragment = this,
            viewModel = viewModel,
            followButton = follow,
            user = User.get(activity),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
        )
    }

    private fun updateStreamLayout(stream: Stream?) {
        if (stream?.viewer_count != null) {
            watchLive.contentDescription = getString(R.string.watch_live)
            watchLive.visible()
        } else {
            if (stream?.lastBroadcast != null) {
                TwitchApiHelper.formatTimeString(requireContext(), stream.lastBroadcast).let {
                    if (it != null) {
                        lastBroadcast.contentDescription =
                            requireContext().getString(R.string.last_broadcast_date, it)
                        lastBroadcast.visible()
                    } else {
                        lastBroadcast.gone()
                    }
                }
            }
        }

        stream?.channelLogo.let {
            if (it != null) {
                userImage.visible()
                userImage.loadImage(this, it, circle = true)
                requireArguments().putString(C.CHANNEL_PROFILEIMAGE, it)
            } else {
                userImage.gone()
            }
        }
        stream?.user_name.let {
            if (it != null && it != requireArguments().getString(C.CHANNEL_DISPLAYNAME)) {
                toolbar.title = it
                requireArguments().putString(C.CHANNEL_DISPLAYNAME, it)
            }
        }
        stream?.user_login.let {
            if (it != null && it != requireArguments().getString(C.CHANNEL_LOGIN)) {
                requireArguments().putString(C.CHANNEL_LOGIN, it)
            }
        }
        if (stream?.title != null) {
            title.visible()
            title.text = stream.title.trim()
        } else {
            title.gone()
        }
        if (stream?.game_name != null) {
            gameName.visible()
            gameName.text = stream.game_name
        } else {
            gameName.gone()
        }
        if (stream?.viewer_count != null) {
            viewers.visible()
            viewers.text = TwitchApiHelper.formatViewersCount(requireContext(), stream.viewer_count)
        } else {
            viewers.gone()
        }
        if (requireContext().prefs().getBoolean(C.UI_UPTIME, true)) {
            if (stream?.started_at != null) {
                TwitchApiHelper.getUptime(requireContext(), stream.started_at).let {
                    if (it != null) {
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
            userImage.visible()
            userImage.loadImage(this, user.channelLogo, circle = true)
            requireArguments().putString(C.CHANNEL_PROFILEIMAGE, user.channelLogo)
        }

        if (user.bannerImageURL != null) {
            bannerImage.loadImage(this, user.bannerImageURL)
        }

        if (requireArguments().getBoolean(C.CHANNEL_UPDATELOCAL)) {
            viewModel.updateLocalUser(requireContext(), user)
        }
    }

    override fun onNetworkRestored() {
        viewModel.retry(
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = requireContext().prefs().getString(C.TOKEN, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
        )
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
