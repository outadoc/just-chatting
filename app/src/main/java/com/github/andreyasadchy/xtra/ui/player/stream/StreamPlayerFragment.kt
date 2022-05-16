package com.github.andreyasadchy.xtra.ui.player.stream

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.ui.player.BasePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.PlayerMode
import com.github.andreyasadchy.xtra.ui.player.PlayerSettingsDialog
import com.github.andreyasadchy.xtra.ui.player.PlayerVolumeDialog
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.player_stream.*

class StreamPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnSortOptionChanged, PlayerSettingsDialog.PlayerSettingsListener, PlayerVolumeDialog.PlayerVolumeListener {

    override val viewModel by viewModels<StreamPlayerViewModel> { viewModelFactory }
    lateinit var chatFragment: ChatFragment
    private lateinit var stream: Stream
    override val channelId: String?
        get() = stream.user_id
    override val channelLogin: String?
        get() = stream.user_login
    override val channelName: String?
        get() = stream.user_name
    override val channelImage: String?
        get() = stream.channelLogo

    override val layoutId: Int
        get() = R.layout.fragment_player_stream
    override val chatContainerId: Int
        get() = R.id.chatFragmentContainer

    override val shouldEnterPictureInPicture: Boolean
        get() = viewModel.playerMode.value == PlayerMode.NORMAL

    override val controllerAutoShow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stream = requireArguments().getParcelable(KEY_STREAM)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatFragment = childFragmentManager.findFragmentById(R.id.chatFragmentContainer).let {
            if (it != null) {
                it as ChatFragment
            } else {
                val fragment = ChatFragment.newInstance(channelId, channelLogin, channelName)
                childFragmentManager.beginTransaction().replace(R.id.chatFragmentContainer, fragment).commit()
                fragment
            }
        }
    }

    override fun initialize() {
        viewModel.startStream(
            user = User.get(requireContext()),
            includeToken = prefs.getBoolean(C.TOKEN_INCLUDE_TOKEN_STREAM, false),
            helixClientId = prefs.getString(C.HELIX_CLIENT_ID, ""),
            gqlClientId = prefs.getString(C.GQL_CLIENT_ID, ""),
            stream = stream,
            useAdBlock = prefs.getBoolean(C.AD_BLOCKER, true),
            randomDeviceId = prefs.getBoolean(C.TOKEN_RANDOM_DEVICEID, true),
            xDeviceId = prefs.getString(C.TOKEN_XDEVICEID, ""),
            deviceId = prefs.getString(C.TOKEN_DEVICEID, ""),
            playerType = prefs.getString(C.TOKEN_PLAYERTYPE, ""),
            minSpeed = prefs.getString(C.PLAYER_LIVE_MIN_SPEED, ""),
            maxSpeed = prefs.getString(C.PLAYER_LIVE_MAX_SPEED, ""),
            targetOffset = prefs.getString(C.PLAYER_LIVE_TARGET_OFFSET, "5000")
        )
        super.initialize()
        val settings = requireView().findViewById<ImageButton>(R.id.playerSettings)
        val playerMenu = requireView().findViewById<ImageButton>(R.id.playerMenu)
        val restart = requireView().findViewById<ImageButton>(R.id.playerRestart)
        val viewersLayout = requireView().findViewById<LinearLayout>(R.id.viewersLayout)
        val viewerIcon = requireView().findViewById<ImageView>(R.id.viewerIcon)
        viewModel.loaded.observe(viewLifecycleOwner) {
            if (it) {
                settings.enable()
                (childFragmentManager.findFragmentByTag("closeOnPip") as? PlayerSettingsDialog?)?.setQualities(viewModel.qualities, viewModel.qualityIndex)
            } else {
                settings.disable()
            }
        }
        viewModel.stream.observe(viewLifecycleOwner) {
            if (it?.viewer_count != null) {
                viewers.text = TwitchApiHelper.formatCount(requireContext(), it.viewer_count)
                if (prefs.getBoolean(C.PLAYER_VIEWERICON, true)) {
                    viewerIcon.visible()
                }
            } else {
                viewers.text = null
                viewerIcon.gone()
            }
        }
        if (prefs.getBoolean(C.PLAYER_SETTINGS, true)) {
            settings.visible()
            settings.setOnClickListener {
                FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities, viewModel.qualityIndex)
            }
        }
        if (prefs.getBoolean(C.PLAYER_MENU, true)) {
            playerMenu.visible()
            playerMenu.setOnClickListener {
                FragmentUtils.showPlayerSettingsDialog(childFragmentManager, if (viewModel.loaded.value == true) viewModel.qualities else null, viewModel.qualityIndex, viewModel.currentPlayer.value!!.playbackParameters.speed)
            }
        }
        if (prefs.getBoolean(C.PLAYER_RESTART, true)) {
            restart.visible()
            restart.setOnClickListener {
                restartPlayer()
            }
        }
        if (prefs.getBoolean(C.PLAYER_VIEWERLIST, false)) {
            viewersLayout.setOnClickListener {
                openViewerList()
            }
        }
    }

    fun restartPlayer() {
        viewModel.restartPlayer()
    }

    fun openViewerList() {
        stream.user_login?.let { login -> FragmentUtils.showPlayerViewerListDialog(childFragmentManager, login, viewModel.repository) }
    }

    override fun changeVolume(volume: Float) {
        viewModel.setVolume(volume)
    }

    fun hideEmotesMenu() = chatFragment.hideEmotesMenu()

    override fun onMinimize() {
        super.onMinimize()
        chatFragment.hideKeyboard()
    }

//    override fun play(obj: Parcelable) {
//        val stream = obj as Stream
//        if (viewModel.stream != stream) {
//            viewModel.player.playWhenReady = false
//            chatView.adapter.submitList(null)
//        }
//        viewModel.stream = stream
//        draggableView?.maximize()
//    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
//            if (index >= viewModel.helper.urls.value!!.lastIndex) {
//                TODO hide player
//            }
    }

    override fun onChangeQuality(index: Int) {
        viewModel.changeQuality(index)
    }

    override fun onChangeSpeed(speed: Float) {}

    override fun onMovedToForeground() {
        viewModel.onResume()
    }

    override fun onMovedToBackground() {
        viewModel.onPause()
    }

    override fun onNetworkRestored() {
        if (isResumed) {
            viewModel.onResume()
        }
    }

    fun startAudioOnly() {
        viewModel.startAudioOnly()
    }

    companion object {
        private const val KEY_STREAM = "stream"

        fun newInstance(stream: Stream): StreamPlayerFragment {
            return StreamPlayerFragment().apply { arguments = bundleOf(KEY_STREAM to stream) }
        }
    }
}
