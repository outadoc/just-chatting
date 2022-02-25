package com.github.andreyasadchy.xtra.ui.player.clip

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.chat.ChatReplayPlayerFragment
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

class ClipPlayerFragment : BasePlayerFragment(), HasDownloadDialog, ChatReplayPlayerFragment, PlayerSettingsDialog.PlayerSettingsListener, PlayerVolumeDialog.PlayerVolumeListener {
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
        if (childFragmentManager.findFragmentById(R.id.chatFragmentContainer) == null && !requireContext().prefs().getBoolean(C.API_USEHELIX, true) || requireContext().prefs().getString(C.USERNAME, "") == "") {
            childFragmentManager.beginTransaction().replace(R.id.chatFragmentContainer, ChatFragment.newInstance(channelId, clip.video_id, clip.videoOffsetSeconds?.toDouble())).commit()
        }
        if (clip.video_id == null || clip.video_id == "") {
            watchVideo.gone()
        }
    }

    override fun initialize() {
        viewModel.setClip(clip)
        super.initialize()
        val view = requireView()
        val settings = view.findViewById<ImageButton>(R.id.settings)
        val download = view.findViewById<ImageButton>(R.id.download)
        view.findViewById<ImageButton>(R.id.gamesButton).gone()
        viewModel.loaded.observe(this) {
            settings.enable()
            download.enable()
        }
        if (prefs.getBoolean(C.PLAYER_DOWNLOAD, true)) {
            download.setOnClickListener { showDownloadDialog() }
        } else {
            download.gone()
        }
        settings.setOnClickListener {
            FragmentUtils.showPlayerSettingsDialog(childFragmentManager, viewModel.qualities.keys, viewModel.qualityIndex, viewModel.currentPlayer.value!!.playbackParameters.speed)
        }
        if (clip.video_id != null && clip.video_id != "") {
            viewModel.video.observe(viewLifecycleOwner) {
                if (it != null) {
                    (requireActivity() as MainActivity).startVideo(it, (clip.videoOffsetSeconds?.toDouble() ?: 0.0) * 1000.0 + viewModel.player.currentPosition)
                }
            }
            watchVideo.setOnClickListener {
                if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
                    viewModel.loadVideo(useHelix = true, clientId = prefs.getString(C.HELIX_CLIENT_ID, ""), token = prefs.getString(C.TOKEN, ""))
                } else {
                    viewModel.loadVideo(useHelix = false, clientId = prefs.getString(C.GQL_CLIENT_ID, ""))
                }
            }
        }
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
