package fr.outadoc.justchatting.ui.chat

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.app.Person
import androidx.core.content.getSystemService
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
import com.google.android.material.composethemeadapter3.Mdc3Theme
import com.google.android.material.shape.MaterialShapeDrawable
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentChannelBinding
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.ensureMinimumAlpha
import fr.outadoc.justchatting.ui.common.isLightColor
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.EmotePicker
import fr.outadoc.justchatting.util.formatChannelUri
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.isDark
import fr.outadoc.justchatting.util.isLaunchedFromBubbleCompat
import fr.outadoc.justchatting.util.loadImageToBitmap
import fr.outadoc.justchatting.util.shortToast
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

object EmojiPickerDefaults {
    val DefaultHeight = 350.dp
}

class ChannelChatFragment : Fragment() {

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

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val statusBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            viewHolder?.apply {
                toolbar.setPadding(
                    toolbar.paddingLeft,
                    statusBarInsets.top,
                    toolbar.paddingRight,
                    toolbar.paddingBottom
                )
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
            }
        }

        viewHolder?.composeViewChat?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val clipboard = LocalClipboardManager.current
                    val haptic = LocalHapticFeedback.current
                    val context = LocalContext.current

                    val state by chatViewModel.state.observeAsState(ChatViewModel.State.Initial)

                    var isEmotePickerOpen by remember { mutableStateOf(false) }

                    BackHandler(isEmotePickerOpen) {
                        isEmotePickerOpen = false
                    }

                    LaunchedEffect(isEmotePickerOpen) {
                        if (isEmotePickerOpen) {
                            keyboardController?.hide()
                        }
                    }

                    Column(verticalArrangement = Arrangement.SpaceEvenly) {
                        ChatScreen(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            state = state,
                            onMessageLongClick = { item ->
                                item.data?.message?.let { rawMessage ->
                                    clipboard.setText(AnnotatedString(rawMessage))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    context.shortToast(R.string.chat_copiedToClipboard)
                                }
                            }
                        )

                        ChatSlowModeProgress(
                            modifier = Modifier.fillMaxWidth(),
                            state = state
                        )

                        val density = LocalDensity.current.density
                        val isDarkTheme = MaterialTheme.colorScheme.isDark

                        ChatInput(
                            modifier = Modifier
                                .padding(8.dp)
                                .then(
                                    if (!isEmotePickerOpen) Modifier.navigationBarsPadding()
                                    else Modifier
                                )
                                .fillMaxWidth(),
                            state = state,
                            onMessageChange = chatViewModel::onMessageInputChanged,
                            onToggleEmotePicker = {
                                isEmotePickerOpen = !isEmotePickerOpen
                            },
                            onEmoteClick = { emote ->
                                chatViewModel.appendEmote(emote, autocomplete = true)
                            },
                            onChatterClick = { chatter ->
                                chatViewModel.appendChatter(chatter, autocomplete = true)
                            },
                            onSubmit = {
                                chatViewModel.submit(
                                    screenDensity = density,
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        )

                        var imeHeight by remember { mutableStateOf(EmojiPickerDefaults.DefaultHeight) }

                        val currentImeHeight = WindowInsets.ime
                            .asPaddingValues()
                            .calculateBottomPadding()

                        LaunchedEffect(currentImeHeight) {
                            if (currentImeHeight > imeHeight) {
                                imeHeight = currentImeHeight
                            }
                        }

                        AnimatedVisibility(visible = isEmotePickerOpen) {
                            EmotePicker(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(imeHeight),
                                onEmoteClick = ::appendEmote,
                                state = state
                            )
                        }
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

    private fun appendEmote(emote: Emote) {
        chatViewModel.appendEmote(emote, autocomplete = false)
    }

    private fun copyToClipboard(chatEntry: ChatEntry) {
        val message = chatEntry.data?.message ?: return
        requireContext()
            .getSystemService<ClipboardManager>()
            ?.setPrimaryClip(
                ClipData.newPlainText("label", message)
            )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewHolder?.appBar?.setExpanded(false, false)
        }
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
