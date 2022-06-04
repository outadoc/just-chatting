package com.github.andreyasadchy.xtra.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.github.andreyasadchy.xtra.R

object FragmentUtils {

    fun showUnfollowDialog(context: Context, channelName: String, positiveCallback: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.unfollow, channelName))
            .setPositiveButton(R.string.yes) { _, _ -> positiveCallback.invoke() }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }
}
