package fr.outadoc.justchatting.ui.follow.channels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.DialogFollowedChannelsSortBinding
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Order.ASC
import fr.outadoc.justchatting.model.helix.follows.Order.DESC
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.model.helix.follows.Sort.ALPHABETICALLY
import fr.outadoc.justchatting.model.helix.follows.Sort.FOLLOWED_AT
import fr.outadoc.justchatting.ui.common.ExpandingBottomSheetDialogFragment

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

    private var viewHolder: DialogFollowedChannelsSortBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnFilter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = DialogFollowedChannelsSortBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        val originalSortId = when (args.getSerializable(SORT) as Sort) {
            FOLLOWED_AT -> R.id.time_followed
            ALPHABETICALLY -> R.id.alphabetically
        }

        val originalOrderId = when (args.getSerializable(ORDER) as Order) {
            DESC -> R.id.newest_first
            ASC -> R.id.oldest_first
        }

        viewHolder?.apply {
            sort.check(originalSortId)
            order.check(originalOrderId)

            apply.setOnClickListener {
                val checkedSortId = sort.checkedRadioButtonId
                val checkedOrderId = order.checkedRadioButtonId

                if (checkedSortId != originalSortId || checkedOrderId != originalOrderId) {
                    val sortBtn = view.findViewById<RadioButton>(checkedSortId)
                    val orderBtn = view.findViewById<RadioButton>(checkedOrderId)
                    listener.onChange(
                        sort = when (checkedSortId) {
                            R.id.time_followed -> FOLLOWED_AT
                            else -> ALPHABETICALLY
                        },
                        sortText = sortBtn.text,
                        order = if (checkedOrderId == R.id.newest_first) DESC else ASC,
                        orderText = orderBtn.text
                    )
                }

                dismiss()
            }
        }
    }
}
