package com.github.exact7.xtra.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.follow.FollowFragment
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.exact7.xtra.ui.player.stream.StreamPlayerFragment
import com.github.exact7.xtra.ui.view.chat.ChatView
import com.github.exact7.xtra.ui.view.SlidingLayout
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.LifecycleListener
import com.github.exact7.xtra.util.Prefs
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.visible
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.player_stream.*

private const val CHAT_OPENED = "ChatOpened"

@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), Injectable, LifecycleListener, SlidingLayout.Listener, FollowFragment {

    private lateinit var slidingLayout: SlidingLayout
    private lateinit var playerView: PlayerView
    private lateinit var chatView: ChatView
    private lateinit var showChat: ImageButton
    private lateinit var hideChat: ImageButton
    protected var isPortrait: Boolean = false
        private set
    protected var shouldHandleLifecycle = true
        private set

    private lateinit var prefs: SharedPreferences
    abstract val channel: Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        prefs = requireActivity().getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.keepScreenOn = true
        val activity = requireActivity() as MainActivity
        slidingLayout = view as SlidingLayout
        slidingLayout.addListener(activity)
        slidingLayout.addListener(this)
        minimize.setOnClickListener { minimize() }
        if (isPortrait) {
            view.findViewById<ImageButton>(R.id.fullscreenEnter).setOnClickListener { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        } else {
            hideStatusBar()
            view.findViewById<ImageButton>(R.id.fullscreenExit).setOnClickListener {
                activity.apply {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }

            }
        }
        playerView = view.findViewById(R.id.playerView)
        if (this !is OfflinePlayerFragment) {
            chatView = view.findViewById(R.id.chatView)
            if (!isPortrait) {
                Prefs.get(requireContext()).getInt(C.LANDSCAPE_CHAT_WIDTH, -1).let {
                    if (it > -1) {
                        chatView.updateLayoutParams { width = it }
                    }
                }
                hideChat = view.findViewById<ImageButton>(R.id.hideChat).apply {
                    setOnClickListener {
                        hideChat()
                        prefs.edit { putBoolean(CHAT_OPENED, false) }
                    }
                }
                showChat = view.findViewById<ImageButton>(R.id.showChat).apply {
                    setOnClickListener {
                        showChat()
                        prefs.edit { putBoolean(CHAT_OPENED, true) }
                    }
                }
                if (prefs.getBoolean(CHAT_OPENED, true)) showChat() else hideChat()
            }
            view.findViewById<ImageButton>(R.id.settings).apply {
                isEnabled = false
                setColorFilter(Color.GRAY)
            }
            view.findViewById<ImageButton>(R.id.profile).setOnClickListener {
                activity.viewChannel(channel)
                slidingLayout.minimize()
            }
            view.findViewById<TextView>(R.id.channel).text = channel.displayName
        }
        if (this !is StreamPlayerFragment) {
            val prefs = Prefs.get(activity)
            val rewind = prefs.getString("playerRewind", "5000")!!.toInt()
            val forward = prefs.getString("playerForward", "5000")!!.toInt()
            val rewindImage = when (rewind) {
                5000 -> R.drawable.baseline_replay_5_black_48
                10000 -> R.drawable.baseline_replay_10_black_48
                else -> R.drawable.baseline_replay_30_black_48
            }
            val forwardImage = when (forward) {
                5000 -> R.drawable.baseline_forward_5_black_48
                10000 -> R.drawable.baseline_forward_10_black_48
                else -> R.drawable.baseline_forward_30_black_48
            }
            view.findViewById<PlayerView>(R.id.playerView).apply {
                setRewindIncrementMs(rewind)
                setFastForwardIncrementMs(forward)
            }
            view.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_rew).setImageResource(rewindImage)
            view.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_ffwd).setImageResource(forwardImage)
            if (this !is OfflinePlayerFragment) {
                view.findViewById<ImageButton>(R.id.download).apply {
                    isEnabled = false
                    setColorFilter(Color.GRAY)
                }
            }
        }
    }

    protected fun initializeViewModel(viewModel: PlayerViewModel, enableChat: Boolean = true) {
        playerView.player = viewModel.player
        if (this !is OfflinePlayerFragment) {
            ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java).user.observe(viewLifecycleOwner, Observer {
                if (it is LoggedIn) {
                    if (enableChat) {
                        chatView.setUsername(it.name)
                    }
                    if (viewModel is FollowViewModel) {
                        initializeFollow(this, viewModel, requireView().findViewById(R.id.follow), it)
                    }
                }
            })
        }
        if (enableChat) {
            viewModel.chatMessages.observe(viewLifecycleOwner, Observer(chatView::submitList))
            viewModel.newMessage.observe(viewLifecycleOwner, Observer { chatView.notifyMessageAdded() })
            val emotesObserver = Observer(chatView::addEmotes)
            viewModel.bttv.observe(viewLifecycleOwner, emotesObserver)
            viewModel.ffz.observe(viewLifecycleOwner, emotesObserver)
        }
    }

//    abstract fun play(obj: Parcelable) //TODO instead maybe add livedata in mainactivity and observe it

    fun minimize() {
        slidingLayout.minimize()
    }

    private fun showStatusBar() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun hideStatusBar() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onMinimize() {
//        if (!isPortrait) { //TODO fix drag view when show status bar
//            showStatusBar()
//        }
    }

    override fun onMaximize() {
//        if (!isPortrait) {
//            hideStatusBar()
//        }
    }

    override fun onClose() {
        if (!isPortrait) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showStatusBar()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        shouldHandleLifecycle = isInPictureInPictureMode //this fires faster than LifecycleListener, so set reverse value
        chatView.visible(!isInPictureInPictureMode)
    }

    private fun hideChat() {
        hideChat.gone()
        showChat.visible()
        chatView.gone()
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
    }

    private fun showChat() {
        hideChat.visible()
        showChat.gone()
        chatView.visible()
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
    }
}
