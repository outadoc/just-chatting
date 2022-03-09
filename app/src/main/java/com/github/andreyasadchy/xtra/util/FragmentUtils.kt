package com.github.andreyasadchy.xtra.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.ui.player.PlayerGamesDialog
import com.github.andreyasadchy.xtra.ui.player.PlayerSettingsDialog
import com.github.andreyasadchy.xtra.ui.player.PlayerViewerListDialog
import com.github.andreyasadchy.xtra.ui.player.PlayerVolumeDialog

object FragmentUtils {

    /**
     * Use this when result should be a string resource id
     */
    fun showRadioButtonDialogFragment(context: Context, fragmentManager: FragmentManager, labels: List<Int>, checkedIndex: Int, requestCode: Int = 0) {
        RadioButtonDialogFragment.newInstance(
            requestCode,
            labels.map(context::getString),
            labels.toIntArray(),
            checkedIndex
        ).show(fragmentManager, null)
    }

    /**
     * Use this when result should be an index
     */
    fun showRadioButtonDialogFragment(fragmentManager: FragmentManager, labels: Collection<CharSequence>, checkedIndex: Int, requestCode: Int = 0) {
        RadioButtonDialogFragment.newInstance(
            requestCode,
            labels,
            null,
            checkedIndex
        ).show(fragmentManager, null)
    }

    fun showUnfollowDialog(context: Context, channelName: String, positiveCallback: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.unfollow, channelName))
            .setPositiveButton(R.string.yes) { _, _ -> positiveCallback.invoke() }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    fun showPlayerSettingsDialog(fragmentManager: FragmentManager, qualities: Collection<CharSequence>, quality: Int, speed: Float) {
        PlayerSettingsDialog.newInstance(
            qualities,
            quality,
            speed
        ).show(fragmentManager, null)
    }

    fun showPlayerVolumeDialog(fragmentManager: FragmentManager) {
        PlayerVolumeDialog.newInstance().show(fragmentManager, null)
    }

    fun showPlayerGamesDialog(fragmentManager: FragmentManager, gamesList: List<Game>) {
        PlayerGamesDialog.newInstance(gamesList).show(fragmentManager, null)
    }

    fun showPlayerViewerListDialog(fragmentManager: FragmentManager, login: String, repository: TwitchService) {
        PlayerViewerListDialog.newInstance(login, repository).show(fragmentManager, null)
    }
}