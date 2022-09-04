package fr.outadoc.justchatting.ui.chat

import android.app.ActivityManager
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.app.Person
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.shape.MaterialShapeDrawable
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentChannelBinding
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.common.ensureMinimumAlpha
import fr.outadoc.justchatting.ui.common.isLightColor
import fr.outadoc.justchatting.ui.view.chat.EmotesFragment
import fr.outadoc.justchatting.ui.view.chat.MessageClickedDialog
import fr.outadoc.justchatting.ui.view.chat.OnEmoteClickedListener
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.util.formatChannelUri
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.hideKeyboard
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.isLaunchedFromBubbleCompat
import fr.outadoc.justchatting.util.loadImageToBitmap
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelChatFragment :
    Fragment(),
    MessageClickedDialog.OnButtonClickListener,
    OnEmoteClickedListener,
    Scrollable {

    companion object {
        private const val CHANNEL_LOGIN = "channel_login"

        fun newInstance(login: String) =
            ChannelChatFragment().apply {
                arguments = bundleOf(CHANNEL_LOGIN to login)
            }
    }

    private val channelViewModel: ChannelChatViewModel by viewModel()
    private val chatViewModel: ChatViewModel by sharedViewModel()

    private var viewHolder: FragmentChannelBinding? = null

    private var openInBubble: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelViewModel.loadStream(
            channelLogin = requireArguments().getString(CHANNEL_LOGIN)!!
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = FragmentChannelBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this) {
            viewHolder?.apply {
                if (!chatInputView.hideEmotesMenu()) {
                    isEnabled = false
                    activity?.onBackPressed()
                    isEnabled = true
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            viewHolder?.apply {
                toolbar.setPadding(
                    toolbar.paddingLeft,
                    insets.top,
                    toolbar.paddingRight,
                    toolbar.paddingBottom
                )
            }
            windowInsets
        }

        channelViewModel.state.observe(viewLifecycleOwner) { state ->
            viewHolder?.apply {
                updateStreamLayout(state.stream)

                state.loadedUser?.let { user ->
                    updateUserLayout(user)

                    chatViewModel.startLive(
                        channelId = user.id,
                        channelLogin = user.login,
                        channelName = user.display_name
                    )
                }

                state.appUser.login?.let { login ->
                    chatView.setUsername(login)
                }

                chatView.showTimestamps = state.showTimestamps
                chatView.animateEmotes = state.animateEmotes
                chatInputView.animateEmotes = state.animateEmotes
            }
        }

        viewHolder?.apply {

            chatView.setOnMessageClickListener { original, formatted, userId ->
                hideKeyboard()

                MessageClickedDialog.newInstance(
                    originalMessage = original,
                    formattedMessage = formatted,
                    userId = userId
                ).show(childFragmentManager, "closeOnPip")
            }

            chatInputView.setOnMessageSendListener { message ->
                chatViewModel.send(
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

            chatInputView.emotePickerAdapter =
                object : FragmentStateAdapter(this@ChannelChatFragment) {

                    override fun createFragment(position: Int): Fragment =
                        EmotesFragment.newInstance(position)

                    override fun getItemCount(): Int = 3
                }
        }

        chatViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ChatViewModel.State.Initial -> {}
                is ChatViewModel.State.Chatting -> {
                    viewHolder?.apply {
                        with(chatInputView) {
                            setMessagePostConstraint(state.messagePostConstraint)
                            setAutocompleteItems(
                                emotes = state.allEmotes,
                                chatters = state.chatters
                            )
                        }

                        with(chatView) {
                            setEmotes(state.allEmotes)
                            submitList(state.chatMessages)
                            notifyRoomState(state.roomState)
                            addGlobalBadges(state.globalBadges)
                            addChannelBadges(state.channelBadges)
                            addCheerEmotes(state.cheerEmotes)
                        }
                    }
                }
            }
        }
    }

    private fun FragmentChannelBinding.updateStreamLayout(stream: Stream?) {
        if (stream?.title != null) {
            toolbar.subtitle = stream.title.trim()
        } else {
            toolbar.subtitle = null
        }
    }

    private fun FragmentChannelBinding.loadUserAvatar(user: User) {
        val context = context ?: return
        val activity = activity ?: return

        val logo = user.profile_image_url ?: return

        lifecycleScope.launch {
            val bitmap = loadImageToBitmap(
                context = context,
                imageUrl = logo,
                circle = true,
                width = 256,
                height = 256
            )

            if (bitmap != null) {
                toolbar.logo = bitmap.createToolbarLogoDrawable()

                val palette = Palette.Builder(bitmap).generateAsync()
                (palette?.dominantSwatch ?: palette?.dominantSwatch)
                    ?.let { swatch ->
                        updateToolbarColor(swatch)
                    }

                activity.setTaskDescription(
                    ActivityManager.TaskDescription(user.display_name, bitmap)
                )

                if (!activity.isLaunchedFromBubbleCompat) {
                    configureChatBubbles(user, bitmap)
                }
            }
        }
    }

    private fun Bitmap.createToolbarLogoDrawable(): Drawable? {
        val context = context ?: return null

        val size = context.resources.getDimension(R.dimen.chat_streamPictureSize).toInt()
        val endMargin = context.resources.getDimension(R.dimen.chat_streamPictureMarginEnd).toInt()

        val bmp = Bitmap.createScaledBitmap(this, size, size, true)

        return InsetDrawable(
            BitmapDrawable(context.resources, bmp),
            /* insetLeft = */ 0,
            /* insetTop = */ 0,
            /* insetRight = */ endMargin,
            /* insetBottom = */ 0
        )
    }

    private fun configureChatBubbles(channel: User, channelLogo: Bitmap) {
        val context = context ?: return

        // Bubbles are only available on Android Q+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        ChatNotificationUtils.createGenericBubbleChannelIfNeeded(context)
            ?: return

        val icon = channelLogo.let { IconCompat.createWithBitmap(it) }

        val person: Person =
            Person.Builder()
                .setKey(channel.id)
                .setName(channel.display_name)
                .setIcon(icon)
                .build()

        ChatNotificationUtils.createShortcutForChannel(
            context = context,
            intent = ChatActivity.createIntent(
                context = context,
                channelLogin = channel.login
            ),
            channelId = channel.id,
            channelName = channel.display_name,
            person = person,
            icon = icon
        )

        viewHolder?.toolbar?.menu
            ?.findItem(R.id.openInBubble)
            ?.isVisible = true

        openInBubble = {
            ChatNotificationUtils.createBubble(
                context = context,
                user = channel,
                icon = icon,
                person = person
            )
        }
    }

    private fun FragmentChannelBinding.updateToolbarColor(swatch: Swatch) {
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

    private fun FragmentChannelBinding.updateUserLayout(user: User) {
        toolbar.apply {
            title = user.display_name

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.watchLive -> {
                        startActivity(
                            Intent(Intent.ACTION_VIEW, formatChannelUri(user.login))
                        )
                        true
                    }
                    R.id.info -> {
                        StreamInfoDialog.newInstance(userId = user.id)
                            .show(childFragmentManager, "closeOnPip")
                        true
                    }
                    R.id.openInBubble -> {
                        openInBubble?.invoke()
                        true
                    }
                    else -> false
                }
            }
        }

        loadUserAvatar(user)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewHolder?.appBar?.setExpanded(false, false)
        }
    }

    private fun hideKeyboard() {
        viewHolder?.apply {
            chatInputView.hideKeyboard()
            chatInputView.clearFocus()
        }
    }

    override fun onEmoteClicked(emote: Emote) {
        viewHolder?.chatInputView?.appendEmote(emote)
    }

    override fun onReplyClicked(userName: String) {
        viewHolder?.chatInputView?.reply(userName)
    }

    override fun onCopyMessageClicked(message: String) {
        viewHolder?.chatInputView?.setMessage(message)
    }

    override fun onViewProfileClicked(login: String) {
        val context = context ?: return
        context.startActivity(
            ChatActivity.createIntent(
                context = context,
                channelLogin = login
            )
        )
    }

    override fun scrollToTop() {
        viewHolder?.appBar?.setExpanded(true, true)
    }

    override fun onPause() {
        super.onPause()
        openInBubble?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
        openInBubble = null
    }
}
