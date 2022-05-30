package com.github.andreyasadchy.xtra.ui.player

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.common.AlertDialogFragment
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.common.follow.FollowFragment
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.player.clip.ClipPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerFragment
import com.github.andreyasadchy.xtra.ui.view.CustomPlayerView
import com.github.andreyasadchy.xtra.ui.view.SlidingLayout
import com.github.andreyasadchy.xtra.util.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.view_chat.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), Injectable, LifecycleListener, SlidingLayout.Listener, FollowFragment, SleepTimerDialog.OnSleepTimerStartedListener, AlertDialogFragment.OnDialogResultListener {

    lateinit var slidingLayout: SlidingLayout
    private lateinit var playerView: CustomPlayerView
    private lateinit var aspectRatioFrameLayout: AspectRatioFrameLayout
    private lateinit var chatLayout: ViewGroup
    private lateinit var fullscreenToggle: ImageButton
    private lateinit var playerAspectRatioToggle: ImageButton
    private lateinit var chatToggle: ImageButton
    private var disableChat: Boolean = false

    protected abstract val layoutId: Int
    protected abstract val chatContainerId: Int

    protected abstract val viewModel: PlayerViewModel

    var isPortrait = false
        private set
    private var isKeyboardShown = false

    protected abstract val shouldEnterPictureInPicture: Boolean
    open val controllerAutoShow: Boolean = true
    open val controllerShowTimeoutMs: Int = 3000
    private var resizeMode = 0

    protected lateinit var prefs: SharedPreferences
    protected abstract val channelId: String?
    protected abstract val channelLogin: String?
    protected abstract val channelName: String?
    protected abstract val channelImage: String?

    val playerWidth: Int
        get() = playerView.width
    val playerHeight: Int
        get() = playerView.height

    private var chatWidthLandscape = 0

    private var systemUiFlags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_FULLSCREEN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        prefs = activity.prefs()
        systemUiFlags = systemUiFlags or (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        isPortrait = activity.isInPortraitOrientation
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false).also {
            (it as LinearLayout).orientation = if (isPortrait) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.keepScreenOn = true
        val activity = requireActivity() as MainActivity
        slidingLayout = view as SlidingLayout
        slidingLayout.addListener(activity)
        slidingLayout.addListener(this)
        slidingLayout.maximizedSecondViewVisibility = if (prefs.getBoolean(C.KEY_CHAT_OPENED, true)) View.VISIBLE else View.GONE //TODO
        playerView = view.findViewById(R.id.playerView)
        chatLayout = view.findViewById(chatContainerId)
        aspectRatioFrameLayout = view.findViewById(R.id.aspectRatioFrameLayout)
        aspectRatioFrameLayout.setAspectRatio(16f / 9f)
        chatWidthLandscape = prefs.getInt(C.LANDSCAPE_CHAT_WIDTH, 0)
        fullscreenToggle = view.findViewById(R.id.playerFullscreenToggle)
        if (prefs.getBoolean(C.PLAYER_FULLSCREEN, true)) {
            fullscreenToggle.visible()
            fullscreenToggle.setOnClickListener {
                activity.apply {
                    if (isPortrait) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
            }
        }
        playerAspectRatioToggle = view.findViewById(R.id.playerAspectRatio)
        if (prefs.getBoolean(C.PLAYER_ASPECT, true)) {
            playerAspectRatioToggle.setOnClickListener {
                setResizeMode()
            }
        }
        chatToggle = view.findViewById(R.id.playerChatToggle)
        disableChat = prefs.getBoolean(C.CHAT_DISABLE, false)
        initLayout()
        playerView.controllerAutoShow = controllerAutoShow
        if (this !is OfflinePlayerFragment) {
            view.findViewById<ImageButton>(R.id.playerSettings).disable()
            if (this !is StreamPlayerFragment) {
                view.findViewById<ImageButton>(R.id.playerDownload).disable()
            }
        }
        if (prefs.getBoolean(C.PLAYER_DOUBLETAP, true) && !disableChat) {
            playerView.setOnDoubleTapListener {
                if (!isPortrait && slidingLayout.isMaximized && this !is OfflinePlayerFragment) {
                    if (chatLayout.isVisible) {
                        hideChat()
                    } else {
                        showChat()
                    }
                    playerView.hideController()
                }
            }
        }
        if (prefs.getBoolean(C.PLAYER_MINIMIZE, true)) {
            view.findViewById<ImageButton>(R.id.playerMinimize).apply {
                visible()
                setOnClickListener { minimize() }
            }
        }
        if (prefs.getBoolean(C.PLAYER_CHANNEL, true)) {
            view.findViewById<TextView>(R.id.playerChannel).apply {
                visible()
                text = channelName
                setOnClickListener {
                    activity.viewChannel(channelId, channelLogin, channelName, channelImage, this@BasePlayerFragment is OfflinePlayerFragment)
                    slidingLayout.minimize()
                }
            }
        }
        if (prefs.getBoolean(C.PLAYER_VOLUMEBUTTON, true)) {
            view.findViewById<ImageButton>(R.id.playerVolume).apply {
                visible()
                setOnClickListener {
                    showVolumeDialog()
                }
            }
        }
        if (this is StreamPlayerFragment) {
            if (!prefs.getBoolean(C.PLAYER_PAUSE, false)) {
                view.findViewById<ImageButton>(R.id.exo_pause).layoutParams.height = 0
            }
            if (User.get(activity) !is NotLoggedIn) {
                if (prefs.getBoolean(C.PLAYER_CHATBARTOGGLE, false) && !disableChat) {
                    view.findViewById<ImageButton>(R.id.playerChatBarToggle).apply {
                        visible()
                        setOnClickListener { toggleChatBar() }
                    }
                }
                slidingLayout.viewTreeObserver.addOnGlobalLayoutListener {
                    if (slidingLayout.isKeyboardShown) {
                        if (!isKeyboardShown) {
                            isKeyboardShown = true
                            if (!isPortrait) {
                                chatLayout.updateLayoutParams { width = (slidingLayout.width / 1.8f).toInt() }
                                showStatusBar()
                            }
                        }
                    } else {
                        if (isKeyboardShown) {
                            isKeyboardShown = false
                            chatLayout.clearFocus()
                            if (!isPortrait) {
                                chatLayout.updateLayoutParams { width = chatWidthLandscape }
                                if (slidingLayout.isMaximized) {
                                    hideStatusBar()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val rewind = prefs.getString(C.PLAYER_REWIND, "10000")!!.toInt()
            val forward = prefs.getString(C.PLAYER_FORWARD, "10000")!!.toInt()
            val rewindImage = when {
                rewind <= 5000 -> R.drawable.baseline_replay_5_black_48
                rewind <= 10000 -> R.drawable.baseline_replay_10_black_48
                else -> R.drawable.baseline_replay_30_black_48
            }
            val forwardImage = when {
                forward <= 5000 -> R.drawable.baseline_forward_5_black_48
                forward <= 10000 -> R.drawable.baseline_forward_10_black_48
                else -> R.drawable.baseline_forward_30_black_48
            }
            view.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_rew).setImageResource(rewindImage)
            view.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_ffwd).setImageResource(forwardImage)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !requireActivity().isInPictureInPictureMode) {
            chatLayout.hideKeyboard()
            chatLayout.clearFocus()
            initLayout()
        }
        (childFragmentManager.findFragmentByTag("closeOnPip") as? PlayerSettingsDialog?)?.dismiss()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        if (isInPictureInPictureMode) {
            playerView.useController = false
            chatLayout.gone()
        } else {
            playerView.useController = true
        }
    }

    override fun initialize() {
        val activity = requireActivity() as MainActivity
        val view = requireView()
        viewModel.currentPlayer.observe(viewLifecycleOwner) {
            playerView.player = it
        }
        viewModel.playerMode.observe(viewLifecycleOwner) {
            if (it == PlayerMode.NORMAL) {
                playerView.controllerHideOnTouch = true
                playerView.controllerShowTimeoutMs = controllerShowTimeoutMs
            } else {
                playerView.controllerHideOnTouch = false
                playerView.controllerShowTimeoutMs = -1
                playerView.showController()
            }
        }
        if (this !is OfflinePlayerFragment && prefs.getBoolean(C.PLAYER_FOLLOW, true) && (requireContext().prefs().getString(C.UI_FOLLOW_BUTTON, "0")?.toInt() ?: 0) < 2) {
            initializeFollow(
                fragment = this,
                viewModel = (viewModel as FollowViewModel),
                followButton = view.findViewById(R.id.playerFollow),
                setting = prefs.getString(C.UI_FOLLOW_BUTTON, "0")?.toInt() ?: 0,
                user = User.get(activity),
                helixClientId = prefs.getString(C.HELIX_CLIENT_ID, ""),
                gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "")
            )
        }
        if (this !is ClipPlayerFragment) {
            viewModel.sleepTimer.observe(viewLifecycleOwner) {
                onMinimize()
                activity.closePlayer()
                if (prefs.getBoolean(C.SLEEP_TIMER_LOCK, true)) {
                    lockScreen()
                }
            }
            if (prefs.getBoolean(C.PLAYER_SLEEP, true)) {
                view.findViewById<ImageButton>(R.id.playerSleepTimer).apply {
                    visible()
                    setOnClickListener {
                        showSleepTimerDialog()
                    }
                }
            }
        }
    }

    override fun onMinimize() {
        playerView.useController = false
        if (!isPortrait) {
            showStatusBar()
            val activity = requireActivity()
            activity.lifecycleScope.launch {
                delay(500L)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    override fun onMaximize() {
        playerView.useController = true
        if (!playerView.controllerHideOnTouch) { //TODO
            playerView.showController()
        }
        if (!isPortrait) {
            hideStatusBar()
        }
    }

    override fun onClose() {

    }

    override fun onSleepTimerChanged(durationMs: Long, hours: Int, minutes: Int, lockScreen: Boolean) {
        val context = requireContext()
        if (durationMs > 0L) {
            context.toast(when {
                hours == 0 -> getString(R.string.playback_will_stop, resources.getQuantityString(R.plurals.minutes, minutes, minutes))
                minutes == 0 -> getString(R.string.playback_will_stop, resources.getQuantityString(R.plurals.hours, hours, hours))
                else -> getString(R.string.playback_will_stop_hours_minutes, resources.getQuantityString(R.plurals.hours, hours, hours), resources.getQuantityString(R.plurals.minutes, minutes, minutes))
            })
        } else if (viewModel.timerTimeLeft > 0L) {
            context.toast(R.string.timer_canceled)
        }
        if (lockScreen != prefs.getBoolean(C.SLEEP_TIMER_LOCK, true)) {
            prefs.edit { putBoolean(C.SLEEP_TIMER_LOCK, lockScreen) }
        }
        viewModel.setTimer(durationMs)
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            REQUEST_FOLLOW -> {
                //TODO
            }
        }
    }

    //    abstract fun play(obj: Parcelable) //TODO instead maybe add livedata in mainactivity and observe it

    fun setResizeMode() {
        resizeMode = (resizeMode + 1).let { if (it < 5) it else 0 }
        playerView.resizeMode = resizeMode
        prefs.edit { putInt(C.ASPECT_RATIO_LANDSCAPE, resizeMode) }
    }

    fun showSleepTimerDialog() {
        SleepTimerDialog.show(childFragmentManager, viewModel.timerTimeLeft)
    }

    fun showVolumeDialog() {
        FragmentUtils.showPlayerVolumeDialog(childFragmentManager)
    }

    fun minimize() {
        slidingLayout.minimize()
    }

    fun maximize() {
        slidingLayout.maximize()
    }

    fun enterPictureInPicture(): Boolean {
        return slidingLayout.isMaximized && shouldEnterPictureInPicture
    }

    private fun initLayout() {
        if (isPortrait) {
            requireActivity().window.decorView.setOnSystemUiVisibilityChangeListener(null)
            aspectRatioFrameLayout.updateLayoutParams<LinearLayout.LayoutParams> {
                width = LinearLayout.LayoutParams.MATCH_PARENT
                height = LinearLayout.LayoutParams.WRAP_CONTENT
                weight = 0f
            }
            chatLayout.updateLayoutParams<LinearLayout.LayoutParams> {
                width = LinearLayout.LayoutParams.MATCH_PARENT
                height = 0
                weight = 1f
            }
            chatLayout.visible()
            if (fullscreenToggle.isVisible) {
                fullscreenToggle.setImageResource(R.drawable.baseline_fullscreen_black_24)
            }
            if (playerAspectRatioToggle.isVisible) {
                playerAspectRatioToggle.gone()
            }
            if (chatToggle.isVisible) {
                chatToggle.gone()
            }
            showStatusBar()
            aspectRatioFrameLayout.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else {
            requireActivity().window.decorView.setOnSystemUiVisibilityChangeListener {
                if (!isKeyboardShown && slidingLayout.isMaximized) {
                    hideStatusBar()
                }
            }
            aspectRatioFrameLayout.updateLayoutParams<LinearLayout.LayoutParams> {
                width = 0
                height = LinearLayout.LayoutParams.MATCH_PARENT
                weight = 1f
            }
            if (this !is OfflinePlayerFragment) {
                chatLayout.updateLayoutParams<LinearLayout.LayoutParams> {
                    width = chatWidthLandscape
                    height = LinearLayout.LayoutParams.MATCH_PARENT
                    weight = 0f
                }
                if (disableChat) {
                    chatLayout.gone()
                    slidingLayout.maximizedSecondViewVisibility = View.GONE
                } else {
                    setPreferredChatVisibility()
                }
                val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
                val btnDown = requireView().findViewById<Button>(R.id.btnDown)
                if (chatLayout.isVisible && btnDown != null && !btnDown.isVisible && recyclerView.adapter?.itemCount != null) {
                    recyclerView.scrollToPosition(recyclerView.adapter?.itemCount!! - 1) // scroll down
                }
            } else {
                chatLayout.gone()
            }
            if (fullscreenToggle.isVisible) {
                fullscreenToggle.setImageResource(R.drawable.baseline_fullscreen_exit_black_24)
            }
            if (playerAspectRatioToggle.hasOnClickListeners()) {
                playerAspectRatioToggle.visible()
            }
            slidingLayout.post {
                if (slidingLayout.isMaximized) {
                    hideStatusBar()
                } else {
                    showStatusBar()
                }
            }
            aspectRatioFrameLayout.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            resizeMode = prefs.getInt(C.ASPECT_RATIO_LANDSCAPE, AspectRatioFrameLayout.RESIZE_MODE_FIT)
        }
        playerView.resizeMode = resizeMode
    }

    private fun setPreferredChatVisibility() {
        if (prefs.getBoolean(C.KEY_CHAT_OPENED, true)) showChat() else hideChat()
    }

    fun toggleChatBar() {
        val messageView = view?.findViewById<LinearLayout>(R.id.messageView)
        if (messageView?.isVisible == true) {
            chatLayout.hideKeyboard()
            chatLayout.clearFocus()
            chatLayout.viewPager.gone() // emote menu
            messageView.gone()
            prefs.edit { putBoolean(C.KEY_CHAT_BAR_VISIBLE, false) }
        } else {
            messageView?.visible()
            prefs.edit { putBoolean(C.KEY_CHAT_BAR_VISIBLE, true) }
        }
    }

    fun hideChat() {
        if (prefs.getBoolean(C.PLAYER_CHATTOGGLE, true)) {
            chatToggle.visible()
            chatToggle.setImageResource(R.drawable.baseline_speaker_notes_black_24)
            chatToggle.setOnClickListener { showChat() }
        }
        chatLayout.gone()
        prefs.edit { putBoolean(C.KEY_CHAT_OPENED, false) }
        slidingLayout.maximizedSecondViewVisibility = View.GONE
    }

    fun showChat() {
        if (prefs.getBoolean(C.PLAYER_CHATTOGGLE, true)) {
            chatToggle.visible()
            chatToggle.setImageResource(R.drawable.baseline_speaker_notes_off_black_24)
            chatToggle.setOnClickListener { hideChat() }
        }
        chatLayout.visible()
        prefs.edit { putBoolean(C.KEY_CHAT_OPENED, true) }
        slidingLayout.maximizedSecondViewVisibility = View.VISIBLE
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
        val btnDown = requireView().findViewById<Button>(R.id.btnDown)
        if (chatLayout.isVisible && btnDown != null && !btnDown.isVisible && recyclerView.adapter?.itemCount != null) {
            recyclerView.scrollToPosition(recyclerView.adapter?.itemCount!! - 1) // scroll down
        }
    }

    private fun showStatusBar() {
        if (isAdded) { //TODO this check might not be needed anymore AND ANDROID 5
            requireActivity().window.decorView.systemUiVisibility = 0
        }
    }

    private fun hideStatusBar() {
        if (isAdded) {
            requireActivity().window.decorView.systemUiVisibility = systemUiFlags
        }
    }

    private fun lockScreen() {
        if ((requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive) {
            try {
                (requireContext().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager).lockNow()
            } catch (e: SecurityException) {

            }
        }
    }

    fun setUserLeaveHint() {
        viewModel.userLeaveHint = true
    }

    fun isPaused(): Boolean {
        return viewModel.isPaused()
    }

    private companion object {
        const val REQUEST_FOLLOW = 0
    }
}
