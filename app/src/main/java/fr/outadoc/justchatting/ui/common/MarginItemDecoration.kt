package fr.outadoc.justchatting.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(
    private val margin: Int,
    private val columnCount: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        outRect.right = margin
        outRect.bottom = margin

        if (position < columnCount) outRect.top = margin
        if (position % columnCount == 0) outRect.left = margin
    }
}
