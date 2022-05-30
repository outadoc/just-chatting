package com.github.andreyasadchy.xtra.ui.player

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.ui.download.HasDownloadDialog
import com.github.andreyasadchy.xtra.ui.player.clip.ClipPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.video.VideoPlayerFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.FragmentUtils
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.player_settings.*

class PlayerSettingsDialog : ExpandingBottomSheetDialogFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    interface PlayerSettingsListener {
        fun onChangeQuality(index: Int)
        fun onChangeSpeed(speed: Float)
    }

    companion object {

        private val SPEEDS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
        private val SPEED_LABELS = listOf(R.string.speed0_25, R.string.speed0_5, R.string.speed0_75, R.string.speed1, R.string.speed1_25, R.string.speed1_5, R.string.speed1_75, R.string.speed2)
        private const val QUALITIES = "qualities"
        private const val QUALITY_INDEX = "quality"
        private const val SPEED = "speed"
        private const val VOD_GAMES = "vod_games"

        private const val REQUEST_CODE_QUALITY = 0
        private const val REQUEST_CODE_SPEED = 1

        fun newInstance(qualities: Collection<CharSequence>?, qualityIndex: Int, speed: Float, vodGames: Boolean): PlayerSettingsDialog {
            return PlayerSettingsDialog().apply {
                arguments = bundleOf(QUALITIES to qualities?.let { ArrayList(it) }, QUALITY_INDEX to qualityIndex, SPEED to speed, VOD_GAMES to vodGames)
            }
        }
    }

    private lateinit var listener: PlayerSettingsListener

    private lateinit var qualities: List<CharSequence>
    private var qualityIndex = 0
    private var speedIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as PlayerSettingsListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.player_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arguments = requireArguments()
        if (parentFragment !is StreamPlayerFragment && requireContext().prefs().getBoolean(C.PLAYER_MENU_SPEED, true)) {
            menuSpeed.visible()
            menuSpeed.setOnClickListener {
                FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, SPEED_LABELS, speedIndex, REQUEST_CODE_SPEED)
            }
            setSelectedSpeed(SPEEDS.indexOf(arguments.getFloat(SPEED)))
        }
        if (requireContext().prefs().getBoolean(C.PLAYER_MENU_QUALITY, false)) {
            menuQuality.visible()
            menuQuality.isEnabled = false
            setQualities(arguments.getCharSequenceArrayList(QUALITIES), arguments.getInt(QUALITY_INDEX))
        }
        if (parentFragment is StreamPlayerFragment) {
            if (requireContext().prefs().getBoolean(C.PLAYER_MENU_VIEWER_LIST, true)) {
                menuViewerList.visible()
                menuViewerList.setOnClickListener {
                    (parentFragment as? StreamPlayerFragment)?.openViewerList()
                    dismiss()
                }
            }
            if (requireContext().prefs().getBoolean(C.PLAYER_MENU_RESTART, false)) {
                menuRestart.visible()
                menuRestart.setOnClickListener {
                    (parentFragment as? StreamPlayerFragment)?.restartPlayer()
                    dismiss()
                }
            }
            if (!requireContext().prefs().getBoolean(C.CHAT_DISABLE, false)) {
                if (User.get(requireParentFragment().requireActivity()) !is NotLoggedIn && requireContext().prefs().getBoolean(C.PLAYER_MENU_CHAT_BAR, true)) {
                    menuChatBar.visible()
                    if (requireContext().prefs().getBoolean(C.KEY_CHAT_BAR_VISIBLE, true)) {
                        menuChatBar.text = requireContext().getString(R.string.hide_chat_bar)
                    } else {
                        menuChatBar.text = requireContext().getString(R.string.show_chat_bar)
                    }
                    menuChatBar.setOnClickListener {
                        (parentFragment as? BasePlayerFragment)?.toggleChatBar()
                        dismiss()
                    }
                }
                if ((parentFragment as? BasePlayerFragment)?.isPortrait == false && requireContext().prefs().getBoolean(C.PLAYER_MENU_CHAT_TOGGLE, false)) {
                    menuChatToggle.visible()
                    if (requireContext().prefs().getBoolean(C.KEY_CHAT_OPENED, true)) {
                        menuChatToggle.text = requireContext().getString(R.string.hide_chat)
                        menuChatToggle.setOnClickListener {
                            (parentFragment as? BasePlayerFragment)?.hideChat()
                            dismiss()
                        }
                    } else {
                        menuChatToggle.text = requireContext().getString(R.string.show_chat)
                        menuChatToggle.setOnClickListener {
                            (parentFragment as? BasePlayerFragment)?.showChat()
                            dismiss()
                        }
                    }
                }
                if ((parentFragment as? StreamPlayerFragment)?.chatFragment?.isActive() == true && requireContext().prefs().getBoolean(C.PLAYER_MENU_CHAT_DISCONNECT, false)) {
                    menuChatDisconnect.visible()
                    menuChatDisconnect.setOnClickListener {
                        (parentFragment as? StreamPlayerFragment)?.chatFragment?.disconnect()
                        dismiss()
                    }
                }
            }
        }
        if (parentFragment is VideoPlayerFragment) {
            if (arguments.getBoolean(VOD_GAMES)) {
                setVodGames()
            }
            if (requireContext().prefs().getBoolean(C.PLAYER_MENU_BOOKMARK, true)) {
                (parentFragment as? VideoPlayerFragment)?.checkBookmark()
                (parentFragment as? VideoPlayerFragment)?.isBookmarked()
                menuBookmark.visible()
                menuBookmark.setOnClickListener {
                    (parentFragment as? VideoPlayerFragment)?.saveBookmark()
                    dismiss()
                }
            }
        }
        if (parentFragment is HasDownloadDialog && requireContext().prefs().getBoolean(C.PLAYER_MENU_DOWNLOAD, true)) {
            menuDownload.visible()
            menuDownload.setOnClickListener {
                (parentFragment as? HasDownloadDialog)?.showDownloadDialog()
                dismiss()
            }
        }
        if (parentFragment !is ClipPlayerFragment && requireContext().prefs().getBoolean(C.PLAYER_MENU_SLEEP, true)) {
            menuTimer.visible()
            menuTimer.setOnClickListener {
                (parentFragment as? BasePlayerFragment)?.showSleepTimerDialog()
                dismiss()
            }
        }
        if ((parentFragment as? BasePlayerFragment)?.isPortrait == false && requireContext().prefs().getBoolean(C.PLAYER_MENU_ASPECT, false)) {
            menuRatio.visible()
            menuRatio.setOnClickListener {
                (parentFragment as? BasePlayerFragment)?.setResizeMode()
                dismiss()
            }
        }
        if (requireContext().prefs().getBoolean(C.PLAYER_MENU_VOLUME, false)) {
            menuVolume.visible()
            menuVolume.setOnClickListener {
                (parentFragment as? BasePlayerFragment)?.showVolumeDialog()
                dismiss()
            }
        }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        when (requestCode) {
            REQUEST_CODE_QUALITY -> {
                listener.onChangeQuality(index)
                setSelectedQuality(index)
            }
            REQUEST_CODE_SPEED -> {
                listener.onChangeSpeed(SPEEDS[index])
                setSelectedSpeed(index)
            }
        }
    }

    private fun setSelectedQuality(index: Int) {
        qualityValue.text = qualities[index]
        qualityIndex = index
    }

    private fun setSelectedSpeed(index: Int) {
        speedValue.text = getString(SPEED_LABELS[index])
        speedIndex = index
    }

    fun setVodGames() {
        if (requireContext().prefs().getBoolean(C.PLAYER_MENU_GAMES, false)) {
            menuVodGames.visible()
            menuVodGames.setOnClickListener {
                (parentFragment as? VideoPlayerFragment)?.view?.findViewById<ImageButton>(R.id.playerGames)?.performClick()
                dismiss()
            }
        }
    }

    fun setBookmarkText(isBookmarked: Boolean) {
        if (isBookmarked) {
            menuBookmark.text = requireContext().getString(R.string.remove_bookmark)
        } else {
            menuBookmark.text = requireContext().getString(R.string.add_bookmark)
        }
    }

    fun setQualities(list: List<CharSequence>?, index: Int) {
        if (!list.isNullOrEmpty() && menuQuality.isVisible) {
            qualities = list
            qualityValue.visible()
            setSelectedQuality(index)
            menuQuality.isEnabled = true
            menuQuality.setOnClickListener {
                FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, qualities, qualityIndex, REQUEST_CODE_QUALITY)
            }
        }
    }
}
