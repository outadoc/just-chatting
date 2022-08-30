package fr.outadoc.justchatting.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import fr.outadoc.justchatting.R

object FragmentUtils {

    fun showUnfollowDialog(context: Context, channelName: String, positiveCallback: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.unfollow, channelName))
            .setPositiveButton(R.string.yes) { _, _ -> positiveCallback.invoke() }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }
}
