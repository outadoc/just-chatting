package com.github.andreyasadchy.xtra.ui.videos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Period.*
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Sort.TIME
import com.github.andreyasadchy.xtra.model.helix.video.Sort.VIEWS
import com.github.andreyasadchy.xtra.ui.clips.common.ClipsFragment
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.ui.videos.channel.ChannelVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.followed.FollowedVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.game.GameVideosFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.FragmentUtils
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.dialog_videos_sort.*

class VideosSortDialog : ExpandingBottomSheetDialogFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    interface OnFilter {
        fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence, type: BroadcastType, languageIndex: Int, saveSort: Boolean)
    }

    companion object {

        private const val SORT = "sort"
        private const val PERIOD = "period"
        private const val TYPE = "type"
        private const val LANGUAGE = "language"
        private const val SAVE_SORT = "save_sort"
        private const val CLIP_CHANNEL = "clip_channel"

        private const val REQUEST_CODE_LANGUAGE = 0

        fun newInstance(sort: Sort? = VIEWS, period: Period? = ALL, type: BroadcastType? = BroadcastType.ALL, languageIndex: Int? = 0, saveSort: Boolean = false, clipChannel: Boolean = false): VideosSortDialog {
            return VideosSortDialog().apply {
                arguments = bundleOf(SORT to sort, PERIOD to period, TYPE to type, LANGUAGE to languageIndex, SAVE_SORT to saveSort, CLIP_CHANNEL to clipChannel)
            }
        }
    }

    private lateinit var listener: OnFilter

    private var langIndex = 0

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
        when (parentFragment) {
            is ClipsFragment -> {
                if (args.getBoolean(CLIP_CHANNEL)) {
                    sort.gone()
                    sortType.gone()
                    selectLang.gone()
                    saveSort.text = requireContext().getString(R.string.save_sort_channel)
                    saveSort.isVisible = parentFragment?.arguments?.getString(C.CHANNEL_ID).isNullOrBlank() == false
                } else {
                    sort.gone()
                    sortType.gone()
                    saveSort.text = requireContext().getString(R.string.save_sort_game)
                    saveSort.isVisible = parentFragment?.arguments?.getString(C.GAME_ID).isNullOrBlank() == false
                }
            }
            is ChannelVideosFragment -> {
                period.gone()
                selectLang.gone()
                saveSort.text = requireContext().getString(R.string.save_sort_channel)
                saveSort.isVisible = parentFragment?.arguments?.getString(C.CHANNEL_ID).isNullOrBlank() == false
            }
            is FollowedVideosFragment -> {
                period.gone()
                selectLang.gone()
                saveSort.gone()
            }
            is GameVideosFragment -> {
                if (requireContext().prefs().getString(C.TOKEN, "").isNullOrBlank()) {
                    period.gone()
                }
                saveSort.text = requireContext().getString(R.string.save_sort_game)
                saveSort.isVisible = parentFragment?.arguments?.getString(C.GAME_ID).isNullOrBlank() == false
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
        val originalLanguageIndex = args.getSerializable(LANGUAGE)
        val originalSaveSort = args.getBoolean(SAVE_SORT)
        sort.check(originalSortId)
        period.check(originalPeriodId)
        sortType.check(originalTypeId)
        langIndex = args.getInt(LANGUAGE)
        saveSort.isChecked = originalSaveSort
        apply.setOnClickListener {
            val checkedPeriodId = period.checkedRadioButtonId
            val checkedSortId = sort.checkedRadioButtonId
            val checkedTypeId = sortType.checkedRadioButtonId
            val checkedSaveSort = saveSort.isChecked
            if (checkedPeriodId != originalPeriodId || checkedSortId != originalSortId || checkedTypeId != originalTypeId || langIndex != originalLanguageIndex || checkedSaveSort != originalSaveSort) {
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
                        },
                        langIndex, checkedSaveSort)
                parentFragment?.scrollTop?.gone()
            }
            dismiss()
        }
        val langArray = resources.getStringArray(R.array.gqlUserLanguageEntries).toList()
        selectLang.setOnClickListener {
            FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, langArray, langIndex, REQUEST_CODE_LANGUAGE)
        }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        when (requestCode) {
            REQUEST_CODE_LANGUAGE -> {
                langIndex = index
            }
        }
    }
}
