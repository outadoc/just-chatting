package fr.outadoc.justchatting.ui.chat

import android.animation.ValueAnimator
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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.Person
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import com.google.android.material.composethemeadapter3.Mdc3Theme
import com.google.android.material.shape.MaterialShapeDrawable
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentChannelBinding
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.ensureMinimumAlpha
import fr.outadoc.justchatting.ui.common.isLightColor
import fr.outadoc.justchatting.ui.view.chat.AutoCompleteAdapter
import fr.outadoc.justchatting.ui.view.chat.AutoCompleteSpaceTokenizer
import fr.outadoc.justchatting.ui.view.chat.MessageClickedDialog
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.ui.view.emotes.EmotePicker
import fr.outadoc.justchatting.util.formatChannelUri
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.hideKeyboard
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.isLaunchedFromBubbleCompat
import fr.outadoc.justchatting.util.loadImageToBitmap
import fr.outadoc.justchatting.util.showKeyboard
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.time.Duration

class ChannelChatFragment : Fragment(), MessageClickedDialog.OnButtonClickListener {

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

    private var autoCompleteAdapter: AutoCompleteAdapter? = null

    private var progressAnimator: ValueAnimator? = null

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
        autoCompleteAdapter = AutoCompleteAdapter(requireContext())
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this) {
            viewHolder?.apply {
                if (!hideEmotesMenu()) {
                    isEnabled = false
                    activity?.onBackPressed()
                    isEnabled = true
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val statusBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            viewHolder?.apply {
                messageView.setPadding(
                    messageView.paddingLeft,
                    messageView.paddingTop,
                    messageView.paddingRight,
                    if (imeInsets.bottom > 0) imeInsets.bottom else navBarInsets.bottom
                )

                toolbar.setPadding(
                    toolbar.paddingLeft,
                    statusBarInsets.top,
                    toolbar.paddingRight,
                    toolbar.paddingBottom
                )

                if (imeInsets.bottom > 0) {
                    // Hide emote picker when keyboard is opened
                    hideEmotesMenu()

                    // Set emote picker height to keyboard height
                    emotePicker.updateLayoutParams {
                        height = imeInsets.bottom
                    }
                }
            }

            windowInsets
        }

        channelViewModel.state.observe(viewLifecycleOwner) { state ->
            viewHolder?.apply {
                toolbar.subtitle = state.stream?.title?.trim()

                state.loadedUser?.let { user ->
                    updateUserLayout(user)

                    chatViewModel.startLive(
                        channelId = user.id,
                        channelLogin = user.login,
                        channelName = user.display_name
                    )
                }

                autoCompleteAdapter?.animateEmotes = state.animateEmotes
            }
        }

        viewHolder?.apply {
            editText.setAdapter(autoCompleteAdapter)
            editText.setTokenizer(AutoCompleteSpaceTokenizer())

            editText.addTextChangedListener(
                afterTextChanged = { text -> send.isVisible = text?.isNotBlank() == true }
            )

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage()
                    true
                } else {
                    false
                }
            }

            textInputLayoutChat.setStartIconOnClickListener {
                // TODO add animation
                if (emotePicker.isGone) {
                    editText.hideKeyboard()
                    emotePicker.isVisible = true
                } else {
                    editText.showKeyboard()
                    emotePicker.isVisible = false
                }
            }

            send.setOnClickListener { sendMessage() }

            messageView.isVisible = true

            emotePicker.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    Mdc3Theme {
                        val state by chatViewModel.state.observeAsState(ChatViewModel.State.Initial)
                        EmotePicker(
                            modifier = Modifier.fillMaxSize(),
                            onEmoteClick = ::appendEmote,
                            state = state
                        )
                    }
                }
            }

            composeViewChat.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    Mdc3Theme {
                        val state by chatViewModel.state.observeAsState(ChatViewModel.State.Initial)
                        ChatScreen(
                            modifier = Modifier.fillMaxSize(),
                            state = state,
                            onMessageClick = { _ ->
                                hideKeyboard()

                                /*
                            MessageClickedDialog.newInstance(
                                originalMessage = original,
                                formattedMessage = formatted,
                                userId = userId
                            ).show(childFragmentManager, "closeOnPip")
                             */
                            }
                        )
                    }
                }
            }
        }

        chatViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ChatViewModel.State.Initial -> {}
                is ChatViewModel.State.Chatting -> {
                    viewHolder?.apply {
                        setMessagePostConstraint(state.messagePostConstraint)

                        autoCompleteAdapter?.submitItems(
                            emotes = state.allEmotes,
                            chatters = state.chatters
                        )
                    }
                }
            }
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
            item.icon?.let { icon ->
                DrawableCompat.setTint(icon, textColor)
            }
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

    private fun FragmentChannelBinding.setMessagePostConstraint(constraint: MessagePostConstraint?) {
        // No constraint, no need for an indicator
        if (constraint == null) {
            progressCannotSendUntil.isInvisible = true
            return
        }

        // If the constraint is in the past, just ignore it
        val now = Clock.System.now()
        val canPostAt = constraint.lastMessageSentAt + constraint.slowModeDuration

        if (canPostAt < now) {
            progressCannotSendUntil.isInvisible = true
            return
        }

        if (progressAnimator != null) {
            progressAnimator?.cancel()
            progressAnimator = null
        }

        progressAnimator = ValueAnimator.ofInt(0, Int.MAX_VALUE)
            .apply {
                interpolator = null
                duration = constraint.slowModeDuration.inWholeMilliseconds

                addUpdateListener { animation ->
                    with(progressCannotSendUntil) {
                        max = Int.MAX_VALUE
                        progress = animation.animatedValue as Int
                        isInvisible = progress == 0
                    }
                }

                reverse()

                currentPlayTime = (now - constraint.lastMessageSentAt).inWholeMilliseconds
            }
    }

    private fun hideEmotesMenu(): Boolean {
        val viewHolder = viewHolder ?: return false
        return if (viewHolder.emotePicker.isVisible) {
            viewHolder.emotePicker.isVisible = false
            true
        } else {
            false
        }
    }

    private fun appendEmote(emote: Emote) {
        viewHolder?.apply {
            editText.text.append(emote.name).append(' ')
        }
    }

    private fun FragmentChannelBinding.sendMessage() {
        val text = editText.text.trim()

        editText.hideKeyboard()
        editText.setText("")
        editText.clearFocus()

        hideEmotesMenu()

        chatViewModel.send(
            message = text,
            screenDensity = requireContext().resources.displayMetrics.density,
            isDarkTheme = requireContext().isDarkMode
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewHolder?.appBar?.setExpanded(false, false)
        }
    }

    private fun hideKeyboard() {
        viewHolder?.apply {
            editText.hideKeyboard()
            editText.clearFocus()
        }
    }

    override fun onReplyClicked(userName: String) {
        val text = "@$userName "
        viewHolder?.editText?.apply {
            setText(text)
            setSelection(text.length)
            showKeyboard()
        }
    }

    override fun onCopyMessageClicked(message: String) {
        viewHolder?.editText?.setText(message)
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

    override fun onPause() {
        super.onPause()
        openInBubble?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
        openInBubble = null
        autoCompleteAdapter = null
    }
}
