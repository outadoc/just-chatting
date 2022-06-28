package com.github.andreyasadchy.xtra.ui.chat

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.palette.graphics.Palette
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.ensureMinimumAlpha
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedDialog
import com.github.andreyasadchy.xtra.ui.view.chat.OnEmoteClickedListener
import com.github.andreyasadchy.xtra.ui.view.chat.StreamInfoDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.FragmentUtils
import com.github.andreyasadchy.xtra.util.LifecycleListener
import com.github.andreyasadchy.xtra.util.hideKeyboard
import com.github.andreyasadchy.xtra.util.isDarkMode
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.shortToast
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.android.synthetic.main.fragment_channel.*

class ChannelChatFragment :
    BaseNetworkFragment(),
    LifecycleListener,
    MessageClickedDialog.OnButtonClickListener,
    OnEmoteClickedListener,
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

    private val channelViewModel by activityViewModels<ChannelChatViewModel> { viewModelFactory }
    private val chatViewModel by activityViewModels<ChatViewModel> { viewModelFactory }

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

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.watchLive -> {
                        args.getString(C.CHANNEL_LOGIN)?.let { login ->
                            startActivity(
                                Intent(Intent.ACTION_VIEW, formatChannelUri(login))
                            )
                        }
                        true
                    }
                    R.id.follow -> with(channelViewModel.follow) {
                        val following = value ?: false
                        if (!following) {
                            saveFollowChannel(context)
                            value = true
                        } else {
                            val channelName = channelViewModel.userName
                            if (channelName != null) {
                                FragmentUtils.showUnfollowDialog(context, channelName) {
                                    deleteFollowChannel(context)
                                    value = false
                                }
                            }
                        }
                        true
                    }
                    R.id.info -> {
                        StreamInfoDialog.newInstance(
                            userId = args.getString(C.CHANNEL_ID)!!
                        ).show(
                            childFragmentManager,
                            "closeOnPip"
                        )
                        true
                    }
                    else -> false
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(this) {
            if (!chatInputView.hideEmotesMenu()) {
                isEnabled = false
                activity?.onBackPressed()
                isEnabled = true
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            toolbar.setPadding(
                toolbar.paddingLeft,
                insets.top,
                toolbar.paddingRight,
                toolbar.paddingBottom
            )
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
            helixToken = requireContext().prefs().getString(C.TOKEN, "")
        )

        viewModel.stream.observe(viewLifecycleOwner) { stream ->
            updateStreamLayout(stream)
            if (stream?.channelUser != null) {
                updateUserLayout(stream.channelUser)
            } else {
                viewModel.loadUser(
                    channelId = requireArguments().getString(C.CHANNEL_ID),
                    helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
                    helixToken = requireContext().prefs().getString(C.TOKEN, "")
                )

                viewModel.user.observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        updateUserLayout(user)
                    }
                }
            }
        }

        initializeFollow(
            user = User.get(requireContext()),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, "")
        )
    }

    private fun initializeChat() = chatViewModel.let { viewModel ->
        val args = requireArguments()
        val prefs = requireContext().prefs()

        val user = User.get(requireContext())
        val userIsLoggedIn = user is LoggedIn

        val maxAdapterCount = prefs.getInt(C.CHAT_LIMIT, 600)

        viewModel.startLive(
            useSSl = true,
            usePubSub = prefs.getBoolean(C.CHAT_PUBSUB_ENABLED, true),
            user = user,
            helixClientId = prefs.getString(C.HELIX_CLIENT_ID, ""),
            channelId = args.getString(C.CHANNEL_ID),
            channelLogin = args.getString(C.CHANNEL_LOGIN),
            channelName = args.getString(C.CHANNEL_DISPLAYNAME),
            showUserNotice = prefs.getBoolean(C.CHAT_SHOW_USERNOTICE, true),
            showClearMsg = prefs.getBoolean(C.CHAT_SHOW_CLEARMSG, true),
            showClearChat = prefs.getBoolean(C.CHAT_SHOW_CLEARCHAT, true),
            enableRecentMsg = prefs.getBoolean(C.CHAT_RECENT, true),
            recentMsgLimit = prefs.getInt(C.CHAT_RECENT_LIMIT, 100),
            maxAdapterCount = maxAdapterCount
        )

        chatView.init(maxAdapterCount)
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

        chatInputView.init(childFragmentManager)

        chatInputView.setOnMessageSendListener { message ->
            viewModel.send(
                message = message,
                animateEmotes = requireContext().prefs().getBoolean(C.ANIMATED_EMOTES, true),
                screenDensity = requireContext().resources.displayMetrics.density,
                isDarkTheme = requireContext().isDarkMode
            )
            chatView.scrollToBottom()
        }

        if (userIsLoggedIn) {
            user.login?.let { chatView.setUsername(it) }

            chatInputView.setChatters(viewModel.chatters)
            viewModel.newChatter.observe(viewLifecycleOwner, chatInputView::addChatter)

            val emotesObserver = { emoteSets: List<EmoteSetItem> ->
                val emotes = emoteSets.mapNotNull { (it as? EmoteSetItem.Emote)?.emote }
                chatView.addEmotes(emotes)
                chatInputView.addEmotes(emotes)
            }

            viewModel.emotesFromSets.observe(viewLifecycleOwner, emotesObserver)
            viewModel.recentEmotes.observe(viewLifecycleOwner, emotesObserver)
            viewModel.otherEmotes.observe(viewLifecycleOwner, emotesObserver)
        }

        chatInputView.enableChatInteraction(userIsLoggedIn)

        viewModel.roomState.observe(viewLifecycleOwner) { chatView.notifyRoomState(it) }
        viewModel.newMessage.observe(viewLifecycleOwner) { chatView.notifyMessageAdded() }
        viewModel.chatMessages.observe(viewLifecycleOwner, chatView::submitList)
        viewModel.recentMessages.observe(viewLifecycleOwner, chatView::addRecentMessages)
        viewModel.globalBadges.observe(viewLifecycleOwner, chatView::addGlobalBadges)
        viewModel.channelBadges.observe(viewLifecycleOwner, chatView::addChannelBadges)
        viewModel.cheerEmotes.observe(viewLifecycleOwner, chatView::addCheerEmotes)
        viewModel.emotesLoaded.observe(viewLifecycleOwner, chatView::notifyEmotesLoaded)
        viewModel.command.observe(viewLifecycleOwner, chatView::notifyCommand)
        viewModel.reward.observe(viewLifecycleOwner, chatView::notifyReward)
    }

    private fun initializeFollow(
        user: User,
        helixClientId: String? = null
    ) {
        with(channelViewModel) {
            setUser(user, helixClientId)
            var initialized = false
            val channelName = userName

            follow.observe(viewLifecycleOwner) { following ->
                if (initialized) {
                    val context = requireContext()
                    context.shortToast(
                        context.getString(
                            if (following) R.string.now_following else R.string.unfollowed,
                            channelName
                        )
                    )
                } else {
                    initialized = true
                }

                toolbar.menu
                    .findItem(R.id.follow)
                    .setIcon(
                        if (following) R.drawable.ic_favorite
                        else R.drawable.ic_favorite_border
                    )
            }
        }
    }

    private fun updateStreamLayout(stream: Stream?) {
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
            toolbar?.subtitle = stream.title.trim()
        } else {
            toolbar?.subtitle = null
        }
    }

    private fun loadUserAvatar(channelLogo: String) {
        requireArguments().putString(C.CHANNEL_PROFILEIMAGE, channelLogo)

        val context = context ?: return
        val size = context.resources.getDimension(R.dimen.chat_streamPictureSize).toInt()
        val endMargin = context.resources.getDimension(R.dimen.chat_streamPictureMarginEnd).toInt()

        loadImage(
            context = context,
            url = channelLogo,
            circle = true,
            width = size,
            height = size
        ) { drawable ->
            toolbar.logo = InsetDrawable(drawable, 0, 0, endMargin, 0)

            (drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                Palette.Builder(bitmap).generate { palette ->
                    val swatch = palette?.dominantSwatch ?: palette?.dominantSwatch
                    swatch?.apply {
                        val backgroundColor = rgb
                        val textColor = ensureMinimumAlpha(
                            background = backgroundColor,
                            foreground = titleTextColor
                        )

                        ViewCompat.setBackground(
                            toolbar,
                            MaterialShapeDrawable.createWithElevationOverlay(
                                toolbar.context,
                                ViewCompat.getElevation(toolbar)
                            ).apply {
                                fillColor = ColorStateList.valueOf(backgroundColor)
                            }
                        )

                        toolbar.setNavigationIconTint(textColor)
                        toolbar.setTitleTextColor(textColor)
                        toolbar.setSubtitleTextColor(textColor)
                        toolbar.menu.forEach { item ->
                            DrawableCompat.setTint(item.icon, textColor)
                        }
                    }
                }
            }
        }
    }

    private fun updateUserLayout(user: com.github.andreyasadchy.xtra.model.helix.user.User) {
        user.channelLogo?.let { channelLogo ->
            requireArguments().putString(C.CHANNEL_PROFILEIMAGE, channelLogo)
            loadUserAvatar(channelLogo)
        }

        if (requireArguments().getBoolean(C.CHANNEL_UPDATELOCAL)) {
            channelViewModel.updateLocalUser(requireContext(), user)
        }
    }

    override fun onNetworkRestored() {
        if (isResumed) {
            chatViewModel.start()
        }

        channelViewModel.retry(
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            helixToken = requireContext().prefs().getString(C.TOKEN, "")
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

    override fun onEmoteClicked(emote: Emote) {
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
        ChatNotificationUtils.openInBubbleOrStartActivity(
            context = requireContext(),
            channelId = id,
            channelLogin = login,
            channelName = name,
            channelLogo = channelLogo
        )
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
    }

    override fun onMovedToBackground() {
    }

    override fun onMovedToForeground() {
    }
}
