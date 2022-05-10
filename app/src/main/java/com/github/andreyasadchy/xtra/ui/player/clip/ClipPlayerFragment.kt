package com.github.andreyasadchy.xtra.ui.player.clip

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.chat.ChatReplayPlayerFragment
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.ui.download.ClipDownloadDialog
import com.github.andreyasadchy.xtra.ui.download.HasDownloadDialog
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.player.BasePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.PlayerSettingsDialog
import com.github.andreyasadchy.xtra.ui.player.PlayerVolumeDialog
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.fragment_player_clip.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ClipPlayerFragment : BasePlayerFragment(), HasDownloadDialog, ChatReplayPlayerFragment, RadioButtonDialogFragment.OnSortOptionChanged, PlayerSettingsDialog.PlayerSettingsListener, PlayerVolumeDialog.PlayerVolumeListener {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override val viewModel by viewModels<ClipPlayerViewModel> { viewModelFactory }
    private lateinit var clip: Clip
    override val channelId: String?
        get() = clip.broadcaster_id
    override val channelLogin: String?
        get() = clip.broadcaster_login
    override val channelName: String?
        get() = clip.broadcaster_name
    override val channelImage: String?
        get() = clip.channelLogo

            override val layoutId: Int
        get() = R.layout.fragment_player_clip
    override val chatContainerId: Int
        get() = R.id.clipChatContainer

    override val shouldEnterPictureInPicture: Boolean
        get() = true

    override val controllerShowTimeoutMs: Int = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clip = requireArguments().getParcelable(KEY_CLIP)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.findFragmentById(R.id.chatFragmentContainer) == null) {
            childFragmentManager.beginTransaction().replace(R.id.chatFragmentContainer, ChatFragment.newInstance(channelId, clip.video_id, clip.videoOffsetSeconds?.toDouble())).commit()
        }
        if (clip.video_id.isNullOrBlank()) {
            watchVideo.gone()
        }
    }

    override fun initialize() {
        viewModel.setClip(clip)
        super.initialize()
        val settings = requireView().findViewById<ImageButton>(R.id.playerSettings)
        val playerMenu = requireView().findViewById<ImageButton>(R.id.playerMenu)
        val download = requireView().findViewById<ImageButton>(R.id.playerDownload)
        viewModel.loaded.observe(this) {
            settings.enable()
            download.enable()
            (childFragmentManager.findFragmentByTag("closeOnPip") as? PlayerSettingsDialog?)?.setQualities(viewModel.qualities.keys.toList(), viewModel.qualityIndex)
        }
        if (prefs.getBoolean(C.PLAYER_SETTINGS, true)) {
            settings.visible()
            settings.setOnClickListener {
                FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities.keys, viewModel.qualityIndex)
            }
        }
        if (prefs.getBoolean(C.PLAYER_MENU, true)) {
            playerMenu.visible()
            playerMenu.setOnClickListener {
                FragmentUtils.showPlayerSettingsDialog(childFragmentManager, viewModel.qualities.keys, viewModel.qualityIndex, viewModel.currentPlayer.value!!.playbackParameters.speed)
            }
        }
        if (prefs.getBoolean(C.PLAYER_DOWNLOAD, false)) {
            download.visible()
            download.setOnClickListener {
                showDownloadDialog()
            }
        }
        if (!clip.video_id.isNullOrBlank()) {
            watchVideo.setOnClickListener {
                (requireActivity() as MainActivity).startVideo(Video(
                    id = clip.video_id!!,
                    user_id = clip.broadcaster_id,
                    user_login = clip.broadcaster_login,
                    user_name = clip.broadcaster_name,
                    profileImageURL = clip.profileImageURL
                ), (if (clip.videoOffsetSeconds != null) {
                    (clip.videoOffsetSeconds?.toDouble() ?: 0.0) * 1000.0 + viewModel.player.currentPosition
                } else {
                    0.0
                }))
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

    override fun showDownloadDialog() {
        if (DownloadUtils.hasStoragePermission(requireActivity())) {
            ClipDownloadDialog.newInstance(clip, viewModel.qualities).show(childFragmentManager, null)
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

    companion object {
        private const val KEY_CLIP = "clip"

        fun newInstance(clip: Clip): ClipPlayerFragment {
            return ClipPlayerFragment().apply { arguments = bundleOf(KEY_CLIP to clip) }
        }
    }
}
