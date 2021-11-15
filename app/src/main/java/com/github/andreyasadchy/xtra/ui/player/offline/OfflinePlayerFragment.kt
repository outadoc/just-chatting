package com.github.andreyasadchy.xtra.ui.player.offline

import android.os.Bundle
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.player.BasePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.PlayerMode
import com.github.andreyasadchy.xtra.ui.player.PlayerSettingsDialog
import com.github.andreyasadchy.xtra.ui.player.PlayerVolumeDialog
import com.github.andreyasadchy.xtra.util.FragmentUtils

class OfflinePlayerFragment : BasePlayerFragment(), PlayerSettingsDialog.PlayerSettingsListener, PlayerVolumeDialog.PlayerVolumeListener {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override val viewModel by viewModels<OfflinePlayerViewModel> { viewModelFactory }
    override val channelId: String
        get() = null!!
    override val channelLogin: String
        get() = null!!
    override val channelName: String
        get() = null!!

    override val layoutId: Int
        get() = R.layout.fragment_player_offline
    override val chatContainerId: Int
        get() = R.id.dummyView

    override val shouldEnterPictureInPicture: Boolean
        get() = viewModel.playerMode.value == PlayerMode.NORMAL

    override val controllerShowTimeoutMs: Int = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        enableNetworkCheck = false
        super.onCreate(savedInstanceState)
    }

    override fun initialize() {
        viewModel.setVideo(requireArguments().getParcelable(KEY_VIDEO)!!)
        super.initialize()
        requireView().findViewById<ImageButton>(R.id.settings).setOnClickListener {
            FragmentUtils.showPlayerSettingsDialog(childFragmentManager, viewModel.qualities, viewModel.qualityIndex, viewModel.currentPlayer.value!!.playbackParameters.speed)
        }
        requireView().findViewById<ImageButton>(R.id.volumeButton).setOnClickListener {
            FragmentUtils.showPlayerVolumeDialog(childFragmentManager)
        }
    }

    override fun onNetworkRestored() {
        //do nothing
    }

    override fun onMovedToForeground() {
        viewModel.onResume()
    }

    override fun onMovedToBackground() {
        viewModel.onPause()
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
    companion object {
        private const val KEY_VIDEO = "video"

        fun newInstance(video: OfflineVideo): OfflinePlayerFragment {
            return OfflinePlayerFragment().apply { arguments = bundleOf(KEY_VIDEO to video) }
        }
    }
}