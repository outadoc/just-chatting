package com.github.andreyasadchy.xtra.ui.videos.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Period.*
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Sort.TIME
import com.github.andreyasadchy.xtra.model.helix.video.Sort.VIEWS
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.ui.videos.channel.ChannelVideosFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.dialog_videos_sort.*

class GameVideosSortDialog : ExpandingBottomSheetDialogFragment() {

    interface OnFilter {
        fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence, type: BroadcastType)
    }

    companion object {

        private const val SORT = "sort"
        private const val PERIOD = "period"
        private const val TYPE = "type"

        fun newInstance(sort: Sort, period: Period, type: BroadcastType): GameVideosSortDialog {
            return GameVideosSortDialog().apply {
                arguments = bundleOf(SORT to sort, PERIOD to period, TYPE to type)
            }
        }
    }

    private lateinit var listener: OnFilter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnFilter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_videos_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        if (parentFragment is ChannelVideosFragment) {
            period.gone()
        } else {
            if (!requireContext().prefs().getBoolean(C.API_USEHELIX, true) || requireContext().prefs().getString(C.USERNAME, "") == "") {
                period.gone()
            }
        }
        val originalSortId = if (args.getSerializable(SORT) as Sort == TIME) R.id.time else R.id.views
        val originalPeriodId = when (args.getSerializable(PERIOD) as Period) {
            DAY -> R.id.today
            WEEK -> R.id.week
            MONTH -> R.id.month
            ALL -> R.id.all
        }
        val originalTypeId = when (args.getSerializable(TYPE) as BroadcastType) {
            BroadcastType.ARCHIVE -> R.id.typeArchive
            BroadcastType.HIGHLIGHT -> R.id.typeHighlight
            BroadcastType.UPLOAD -> R.id.typeUpload
            BroadcastType.ALL -> R.id.typeAll
        }
        sort.check(originalSortId)
        period.check(originalPeriodId)
        sortType.check(originalTypeId)
        apply.setOnClickListener {
            val checkedPeriodId = period.checkedRadioButtonId
            val checkedSortId = sort.checkedRadioButtonId
            val checkedTypeId = sortType.checkedRadioButtonId
            if (checkedPeriodId != originalPeriodId || checkedSortId != originalSortId || checkedTypeId != originalTypeId) {
                val sortBtn = view.findViewById<RadioButton>(checkedSortId)
                val periodBtn = view.findViewById<RadioButton>(checkedPeriodId)
                listener.onChange(
                        if (checkedSortId == R.id.time) TIME else VIEWS,
                        sortBtn.text,
                        when (checkedPeriodId) {
                            R.id.today -> DAY
                            R.id.week -> WEEK
                            R.id.month -> MONTH
                            else -> ALL
                        },
                        periodBtn.text,
                        when (checkedTypeId) {
                            R.id.typeArchive -> BroadcastType.ARCHIVE
                            R.id.typeHighlight -> BroadcastType.HIGHLIGHT
                            R.id.typeUpload -> BroadcastType.UPLOAD
                            else -> BroadcastType.ALL
                        })
            }
            dismiss()
        }
    }
}
