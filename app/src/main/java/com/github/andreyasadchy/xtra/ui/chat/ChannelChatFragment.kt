package com.github.andreyasadchy.xtra.ui.chat

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.LifecycleListener
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.hideKeyboard
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_channel.appBar
import kotlinx.android.synthetic.main.fragment_channel.bannerImage
import kotlinx.android.synthetic.main.fragment_channel.chatInputView
import kotlinx.android.synthetic.main.fragment_channel.chatView
import kotlinx.android.synthetic.main.fragment_channel.follow
import kotlinx.android.synthetic.main.fragment_channel.gameName
import kotlinx.android.synthetic.main.fragment_channel.lastBroadcast
import kotlinx.android.synthetic.main.fragment_channel.spacerTop
import kotlinx.android.synthetic.main.fragment_channel.title
import kotlinx.android.synthetic.main.fragment_channel.toolbar
import kotlinx.android.synthetic.main.fragment_channel.uptime
import kotlinx.android.synthetic.main.fragment_channel.userCreated
import kotlinx.android.synthetic.main.fragment_channel.userFollowers
import kotlinx.android.synthetic.main.fragment_channel.userImage
import kotlinx.android.synthetic.main.fragment_channel.userViews
import kotlinx.android.synthetic.main.fragment_channel.viewers
import kotlinx.android.synthetic.main.fragment_channel.watchLive
import kotlinx.coroutines.launch

class ChannelChatFragment :
    BaseNetworkFragment(),
    LifecycleListener,
    MessageClickedDialog.OnButtonClickListener,
    FollowFragment,
    Scrollable {

    companion object {
        private const val ARG_SHOW_BACK_BUTTON = "show_back_button"

        fun newInstance(
            id: String?,
            login: String?,
            name: String?,
            channelLogo: String?,
            showBackButton: Boolean,
            updateLocal: Boolean = false
        ) = ChannelChatFragment().apply {
            arguments = Bundle().apply {
                putString(C.CHANNEL_ID, id)
                putString(C.CHANNEL_LOGIN, login)
                putString(C.CHANNEL_DISPLAYNAME, name)
                putString(C.CHANNEL_PROFILEIMAGE, channelLogo)
                putBoolean(C.CHANNEL_UPDATELOCAL, updateLocal)
                putBoolean(ARG_SHOW_BACK_BUTTON, showBackButton)
            }
        }
    }

    private val channelViewModel by viewModels<ChannelChatViewModel> { viewModelFactory }
    private val chatViewModel by viewModels<ChatViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()

        toolbar.apply {
            title = args.getString(C.CHANNEL_DISPLAYNAME)

            if (args.getBoolean(ARG_SHOW_BACK_BUTTON)) {
                setNavigationIcon(R.drawable.ic_back)
            }

            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        watchLive.setOnClickListener {
            args.getString(C.CHANNEL_LOGIN)?.let { login ->
                startActivity(
                    Intent(Intent.ACTION_VIEW, formatChannelUri(login))
                )
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            spacerTop.updateLayoutParams<MarginLayoutParams> {
                height = insets.top
            }
            windowInsets
        }
    }

    private fun formatChannelUri(channelLogin: String): Uri {
        return Uri.parse("https://twitch.tv")
            .buildUpon()
            .appendPath(channelLogin)
            .build()
    }

    override fun initialize() {
        initializeChannel()
        initializeChat()
    }

    private fun initializeChannel() = channelViewModel.let { viewModel ->
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
            viewModel = channelViewModel,
            followButton = follow,
            user = User.get(requireContext()),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
        )
    }

    private fun initializeChat() = chatViewModel.let { viewModel ->
        val args = requireArguments()
        val prefs = requireContext().prefs()

        val user = User.get(requireContext())
        val userIsLoggedIn = user is LoggedIn

        viewModel.startLive(
            useSSl = true,
            usePubSub = prefs.getBoolean(C.CHAT_PUBSUB_ENABLED, true),
            user = user,
            helixClientId = prefs.getString(C.HELIX_CLIENT_ID, ""),
            gqlClientId = prefs.getString(C.GQL_CLIENT_ID, "") ?: "",
            channelId = args.getString(C.CHANNEL_ID),
            channelLogin = args.getString(C.CHANNEL_LOGIN),
            channelName = args.getString(C.CHANNEL_DISPLAYNAME),
            showUserNotice = prefs.getBoolean(C.CHAT_SHOW_USERNOTICE, true),
            showClearMsg = prefs.getBoolean(C.CHAT_SHOW_CLEARMSG, true),
            showClearChat = prefs.getBoolean(C.CHAT_SHOW_CLEARCHAT, true),
            enableRecentMsg = prefs.getBoolean(C.CHAT_RECENT, true),
            recentMsgLimit = prefs.getInt(C.CHAT_RECENT_LIMIT, 100)
        )

        chatView.init(this)
        chatView.setOnMessageClickListener { original, formatted, userId, fullMsg ->
            hideKeyboard()

            MessageClickedDialog.newInstance(
                messagingEnabled = true,
                originalMessage = original,
                formattedMessage = formatted,
                userId = userId,
                fullMsg = fullMsg
            ).show(childFragmentManager, "closeOnPip")
        }

        chatInputView.init(this)

        chatInputView.setOnMessageSendListener { message ->
            viewModel.send(message)
            chatView.scrollToBottom()
        }

        if (userIsLoggedIn) {
            user.login?.let { chatView.setUsername(it) }
            chatInputView.setChatters(viewModel.chatters)

            val emotesObserver = { emotes: List<Emote> ->
                chatView.addEmotes(emotes)
                chatInputView.addEmotes(emotes)
            }
            viewModel.emotesFromSets.observe(viewLifecycleOwner, emotesObserver)
            viewModel.recentEmotes.observe(viewLifecycleOwner, emotesObserver)

            viewModel.newChatter.observe(viewLifecycleOwner, Observer(chatInputView::addChatter))
        }

        chatInputView.enableChatInteraction(userIsLoggedIn)

        viewModel.roomState.observe(viewLifecycleOwner) { chatInputView.notifyRoomState(it) }

        viewModel.chatMessages.observe(viewLifecycleOwner, Observer(chatView::submitList))
        viewModel.newMessage.observe(viewLifecycleOwner) { chatView.notifyMessageAdded() }
        viewModel.recentMessages.observe(viewLifecycleOwner) { chatView.addRecentMessages(it) }
        viewModel.globalBadges.observe(viewLifecycleOwner, Observer(chatView::addGlobalBadges))
        viewModel.channelBadges.observe(viewLifecycleOwner, Observer(chatView::addChannelBadges))
        viewModel.otherEmotes.observe(viewLifecycleOwner, Observer(chatView::addEmotes))
        viewModel.cheerEmotes.observe(viewLifecycleOwner, Observer(chatView::addCheerEmotes))
        viewModel.emotesLoaded.observe(viewLifecycleOwner) { chatView.notifyEmotesLoaded() }
        viewModel.command.observe(viewLifecycleOwner) { chatView.notifyCommand(it) }
        viewModel.reward.observe(viewLifecycleOwner) { chatView.notifyReward(it) }
    }

    private fun updateStreamLayout(stream: Stream?) {
        if (stream?.viewer_count != null) {
            watchLive.contentDescription = getString(R.string.watch_live)
            watchLive.visible()
        } else {
            if (stream?.lastBroadcast != null) {
                TwitchApiHelper.formatTimeString(requireContext(), stream.lastBroadcast).let {
                    if (it != null) {
                        lastBroadcast.text =
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
            channelViewModel.updateLocalUser(requireContext(), user)
        }

        if (user.created_at != null) {
            userCreated.visible()
            userCreated.text = requireContext().getString(
                R.string.created_at,
                TwitchApiHelper.formatTimeString(requireContext(), user.created_at)
            )
        }

        if (user.followers_count != null) {
            userFollowers.visible()
            userFollowers.text = requireContext().getString(
                R.string.followers,
                TwitchApiHelper.formatCount(requireContext(), user.followers_count)
            )
        }

        if (user.view_count != null) {
            userViews.visible()
            userViews.text = TwitchApiHelper.formatViewsCount(requireContext(), user.view_count)
        }
    }

    override fun onNetworkRestored() {
        if (isResumed) {
            chatViewModel.start()
        }

        channelViewModel.retry(
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

    private fun hideKeyboard() {
        chatInputView.hideKeyboard()
        chatInputView.clearFocus()
    }

    fun hideEmotesMenu(): Boolean {
        return chatInputView.hideEmotesMenu()
    }

    fun appendEmote(emote: Emote) {
        chatInputView.appendEmote(emote)
    }

    override fun onReplyClicked(userName: String) {
        chatInputView.reply(userName)
    }

    override fun onCopyMessageClicked(message: String) {
        chatInputView.setMessage(message)
    }

    override fun onViewProfileClicked(
        id: String?,
        login: String?,
        name: String?,
        channelLogo: String?
    ) {
        if (id == null || login == null || name == null || channelLogo == null) return
        lifecycleScope.launch {
            ChatNotificationUtils.openInBubbleOrStartActivity(
                context = requireContext(),
                channelId = id,
                channelLogin = login,
                channelName = name,
                channelLogo = channelLogo
            )
        }
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
    }

    override fun onMovedToBackground() {
    }

    override fun onMovedToForeground() {
    }
}
