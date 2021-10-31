package com.github.andreyasadchy.xtra.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class ExpandingBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheet.setBackgroundColor(resources.getColor(
            when (requireContext().prefs().getString(C.THEME, "0")) {
                "1" -> R.color.primaryAmoled
                "2" -> R.color.primaryLight
                "3" -> R.color.primaryBlue
                else -> R.color.primaryDark
            }))
        }
        return dialog
    }
}