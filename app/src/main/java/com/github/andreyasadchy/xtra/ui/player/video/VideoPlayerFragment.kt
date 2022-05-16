package com.github.andreyasadchy.xtra.ui.player.video

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.chat.ChatReplayPlayerFragment
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.ui.download.HasDownloadDialog
import com.github.andreyasadchy.xtra.ui.download.VideoDownloadDialog
import com.github.andreyasadchy.xtra.ui.player.*
import com.github.andreyasadchy.xtra.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class VideoPlayerFragment : BasePlayerFragment(), HasDownloadDialog, ChatReplayPlayerFragment, RadioButtonDialogFragment.OnSortOptionChanged, PlayerSettingsDialog.PlayerSettingsListener, PlayerVolumeDialog.PlayerVolumeListener, PlayerGamesDialog.PlayerSeekListener {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override val viewModel by viewModels<VideoPlayerViewModel> { viewModelFactory }
    private lateinit var video: Video
    override val channelId: String?
        get() = video.user_id
    override val channelLogin: String?
        get() = video.user_login
    override val channelName: String?
        get() = video.user_name
    override val channelImage: String?
        get() = video.channelLogo

    override val layoutId: Int
        get() = R.layout.fragment_player_video
    override val chatContainerId: Int
        get() = R.id.chatFragmentContainer

    override val shouldEnterPictureInPicture: Boolean
        get() = viewModel.playerMode.value == PlayerMode.NORMAL

    override val controllerShowTimeoutMs: Int = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        video = requireArguments().getParcelable(KEY_VIDEO)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.findFragmentById(R.id.chatFragmentContainer) == null) {
            childFragmentManager.beginTransaction().replace(R.id.chatFragmentContainer, ChatFragment.newInstance(channelId, video.id, 0.0)).commit()
        }
    }

    override fun initialize() {
        viewModel.setVideo(
            gqlClientId = prefs.getString(C.GQL_CLIENT_ID, ""),
            gqlToken = if (prefs.getBoolean(C.TOKEN_INCLUDE_TOKEN_VIDEO, true)) User.get(requireContext()).gqlToken else null,
            video = video,
            offset = requireArguments().getDouble(KEY_OFFSET)
        )
        super.initialize()
        val settings = requireView().findViewById<ImageButton>(R.id.playerSettings)
        val playerMenu = requireView().findViewById<ImageButton>(R.id.playerMenu)
        val download = requireView().findViewById<ImageButton>(R.id.playerDownload)
        val gamesButton = requireView().findViewById<ImageButton>(R.id.playerGames)
        viewModel.loaded.observe(viewLifecycleOwner) {
            if (it) {
                settings.enable()
                download.enable()
                (childFragmentManager.findFragmentByTag("closeOnPip") as? PlayerSettingsDialog?)?.setQualities(viewModel.qualities, viewModel.qualityIndex)
            } else {
                download.disable()
                settings.disable()
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
                FragmentUtils.showPlayerSettingsDialog(childFragmentManager, if (viewModel.loaded.value == true) viewModel.qualities else null, viewModel.qualityIndex, viewModel.currentPlayer.value!!.playbackParameters.speed, !viewModel.gamesList.value.isNullOrEmpty())
            }
        }
        if (prefs.getBoolean(C.PLAYER_DOWNLOAD, false)) {
            download.visible()
            download.setOnClickListener {
                showDownloadDialog()
            }
        }
        if (prefs.getBoolean(C.PLAYER_GAMESBUTTON, true)) {
            viewModel.loadGamesList(prefs.getString(C.GQL_CLIENT_ID, ""), video.id).observe(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    gamesButton.visible()
                    gamesButton.setOnClickListener { FragmentUtils.showPlayerGamesDialog(childFragmentManager, list) }
                    (childFragmentManager.findFragmentByTag("closeOnPip") as? PlayerSettingsDialog?)?.setVodGames()
                }
            }
        }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }

    override fun onChangeQuality(index: Int) {
        viewModel.changeQuality(index)
    }

    override fun onChangeSpeed(speed: Float) {
        viewModel.setSpeed(speed)
    }

    override fun changeVolume(volume: Float) {
        viewModel.setVolume(volume)
    }

    override fun seek(position: Long) {
        viewModel.seek(position)
    }

    override fun showDownloadDialog() {
        if (DownloadUtils.hasStoragePermission(requireActivity())) {
            viewModel.videoInfo?.let { VideoDownloadDialog.newInstance(it).show(childFragmentManager, null) }
        }
    }

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

    override fun onNetworkLost() {
        if (isResumed) {
            viewModel.onPause()
        }
    }

    override fun getCurrentPosition(): Double {
        return runBlocking(Dispatchers.Main) { viewModel.currentPlayer.value!!.currentPosition / 1000.0 }
    }

    fun startAudioOnly() {
        viewModel.startAudioOnly()
    }

    companion object {
        private const val KEY_VIDEO = "video"
        private const val KEY_OFFSET = "offset"

        fun newInstance(video: Video, offset: Double? = null): VideoPlayerFragment {
            return VideoPlayerFragment().apply { arguments = bundleOf(KEY_VIDEO to video, KEY_OFFSET to offset) }
        }
    }
}
