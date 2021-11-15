package com.github.andreyasadchy.xtra.ui.follow.channels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment

class FollowedChannelsSortDialog : ExpandingBottomSheetDialogFragment() {

    interface OnFilter {
        fun onChange(sortText: CharSequence, orderText: CharSequence)
    }

    companion object {

        fun newInstance(): FollowedChannelsSortDialog {
            return FollowedChannelsSortDialog().apply {

            }
        }
    }

    private lateinit var listener: OnFilter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnFilter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_followed_channels_sort, container, false)
    }
}