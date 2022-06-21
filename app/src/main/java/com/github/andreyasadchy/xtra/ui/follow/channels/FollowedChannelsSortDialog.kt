package com.github.andreyasadchy.xtra.ui.follow.channels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Order.ASC
import com.github.andreyasadchy.xtra.model.helix.follows.Order.DESC
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.model.helix.follows.Sort.ALPHABETICALLY
import com.github.andreyasadchy.xtra.model.helix.follows.Sort.FOLLOWED_AT
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_followed_channels_sort.apply
import kotlinx.android.synthetic.main.dialog_followed_channels_sort.order
import kotlinx.android.synthetic.main.dialog_followed_channels_sort.sort

class FollowedChannelsSortDialog : ExpandingBottomSheetDialogFragment() {

    interface OnFilter {
        fun onChange(sort: Sort, sortText: CharSequence, order: Order, orderText: CharSequence)
    }

    companion object {

        private const val SORT = "sort"
        private const val ORDER = "order"

        fun newInstance(sort: Sort, order: Order): FollowedChannelsSortDialog {
            return FollowedChannelsSortDialog().apply {
                arguments = bundleOf(SORT to sort, ORDER to order)
            }
        }
    }

    private lateinit var listener: OnFilter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnFilter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_followed_channels_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val originalSortId = when (args.getSerializable(SORT) as Sort) {
            FOLLOWED_AT -> R.id.time_followed
            ALPHABETICALLY -> R.id.alphabetically
        }

        val originalOrderId =
            if (args.getSerializable(ORDER) as Order == DESC) R.id.newest_first else R.id.oldest_first

        sort.check(originalSortId)
        order.check(originalOrderId)
        apply.setOnClickListener {
            val checkedSortId = sort.checkedRadioButtonId
            val checkedOrderId = order.checkedRadioButtonId
            if (checkedSortId != originalSortId || checkedOrderId != originalOrderId) {
                val sortBtn = view.findViewById<RadioButton>(checkedSortId)
                val orderBtn = view.findViewById<RadioButton>(checkedOrderId)
                listener.onChange(
                    when (checkedSortId) {
                        R.id.time_followed -> FOLLOWED_AT
                        else -> ALPHABETICALLY
                    },
                    sortBtn.text,
                    if (checkedOrderId == R.id.newest_first) DESC else ASC,
                    orderBtn.text
                )
            }
            dismiss()
        }
    }
}
