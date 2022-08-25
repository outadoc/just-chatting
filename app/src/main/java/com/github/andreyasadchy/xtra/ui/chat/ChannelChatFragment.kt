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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.ensureMinimumAlpha
import com.github.andreyasadchy.xtra.ui.common.isLightColor
import com.github.andreyasadchy.xtra.ui.view.chat.EmotesFragment
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedDialog
import com.github.andreyasadchy.xtra.ui.view.chat.OnEmoteClickedListener
import com.github.andreyasadchy.xtra.ui.view.chat.StreamInfoDialog
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.FragmentUtils
import com.github.andreyasadchy.xtra.util.LifecycleListener
import com.github.andreyasadchy.xtra.util.hideKeyboard
import com.github.andreyasadchy.xtra.util.isDarkMode
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.shortToast
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.android.synthetic.main.fragment_channel.appBar
import kotlinx.android.synthetic.main.fragment_channel.chatInputView
import kotlinx.android.synthetic.main.fragment_channel.chatView
import kotlinx.android.synthetic.main.fragment_channel.toolbar
import javax.inject.Inject

class ChannelChatFragment :
    Fragment(),
    LifecycleListener,
    MessageClickedDialog.OnButtonClickListener,
    OnEmoteClickedListener,
    Scrollable,
    Injectable {

    companion object {
        private const val ARG_SHOW_BACK_BUTTON = "show_back_button"

        fun newInstance(
            id: String?,
            login: String?,
            name: String?,
            channelLogo: String?,
            showBackButton: Boolean,
            updateLocal: Boolean = false,
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

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val channelViewModel by activityViewModels<ChannelChatViewModel> { viewModelFactory }
    private val chatViewModel by activityViewModels<ChatViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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

        initializeChannel()
        initializeChat()

        channelViewModel.user.observe(viewLifecycleOwner) { user ->
            initializeFollow(user)

            user.login?.let { login ->
                chatView.setUsername(login)
            }
        }

        channelViewModel.state.observe(viewLifecycleOwner) { state ->
            chatView?.showTimestamps = state.showTimestamps
            chatView?.animateEmotes = state.animateEmotes
            chatInputView?.animateEmotes = state.animateEmotes
        }
    }

    private fun formatChannelUri(channelLogin: String): Uri {
        return Uri.parse("https://twitch.tv")
            .buildUpon()
            .appendPath(channelLogin)
            .build()
    }

    private fun initializeChannel() = channelViewModel.let { viewModel ->
        viewModel.loadStream(
            channelId = requireArguments().getString(C.CHANNEL_ID),
            channelLogin = requireArguments().getString(C.CHANNEL_LOGIN),
            channelName = requireArguments().getString(C.CHANNEL_DISPLAYNAME),
            profileImageURL = requireArguments().getString(C.CHANNEL_PROFILEIMAGE)
        )

        viewModel.stream.observe(viewLifecycleOwner) { stream ->
            updateStreamLayout(stream)
        }

        viewModel.loadedUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                updateUserLayout(user)
            }
        }
    }

    private fun initializeChat() = chatViewModel.let { viewModel ->
        val args = requireArguments()

        viewModel.startLive(
            channelId = args.getString(C.CHANNEL_ID),
            channelLogin = args.getString(C.CHANNEL_LOGIN),
            channelName = args.getString(C.CHANNEL_DISPLAYNAME)
        )

        chatView.setOnMessageClickListener { original, formatted, userId ->
            hideKeyboard()

            MessageClickedDialog.newInstance(
                messagingEnabled = true,
                originalMessage = original,
                formattedMessage = formatted,
                userId = userId
            ).show(childFragmentManager, "closeOnPip")
        }

        chatInputView.setOnMessageSendListener { message ->
            viewModel.send(
                message = message,
                screenDensity = requireContext().resources.displayMetrics.density,
                isDarkTheme = requireContext().isDarkMode
            )

            chatView.scrollToBottom()
        }

        chatInputView.emotePickerSelectedTab.observe(viewLifecycleOwner) { position ->
            val fragment = childFragmentManager.findFragmentByTag("f$position")
            (fragment as? Scrollable)?.scrollToTop()
        }

        chatInputView.emotePickerAdapter = object : FragmentStateAdapter(this) {

            override fun createFragment(position: Int): Fragment =
                EmotesFragment.newInstance(position)

            override fun getItemCount(): Int = 3
        }

        viewModel.chatters.observe(viewLifecycleOwner, chatInputView::setChatters)
        viewModel.messagePostConstraint.observe(
            viewLifecycleOwner,
            chatInputView::setMessagePostConstraint
        )

        val emotesObserver = { emoteSets: List<EmoteSetItem> ->
            val emotes = emoteSets.mapNotNull { (it as? EmoteSetItem.Emote)?.emote }
            chatView.addEmotes(emotes)
            chatInputView.addEmotes(emotes)
        }

        viewModel.emotesFromSets.observe(viewLifecycleOwner, emotesObserver)
        viewModel.recentEmotes.observe(viewLifecycleOwner, emotesObserver)
        viewModel.otherEmotes.observe(viewLifecycleOwner, emotesObserver)
        viewModel.roomState.observe(viewLifecycleOwner) { chatView.notifyRoomState(it) }
        viewModel.chatMessages.observe(viewLifecycleOwner, chatView::submitList)
        viewModel.globalBadges.observe(viewLifecycleOwner, chatView::addGlobalBadges)
        viewModel.channelBadges.observe(viewLifecycleOwner, chatView::addChannelBadges)
        viewModel.cheerEmotes.observe(viewLifecycleOwner, chatView::addCheerEmotes)
        viewModel.emotesLoaded.observe(viewLifecycleOwner, chatView::notifyEmotesLoaded)
    }

    private fun initializeFollow(user: User) {
        with(channelViewModel) {
            setUser(user)
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
                    (palette?.dominantSwatch ?: palette?.dominantSwatch)
                        ?.let { swatch -> updateToolbarColor(swatch) }
                }
            }
        }
    }

    private fun updateToolbarColor(swatch: Swatch) {
        val backgroundColor = swatch.rgb
        val textColor = ensureMinimumAlpha(
            foreground = swatch.titleTextColor,
            background = backgroundColor
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

        activity?.let { activity ->
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                .isAppearanceLightStatusBars = !textColor.isLightColor
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
        channelLogo: String?,
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
