package com.github.andreyasadchy.xtra.ui.view

import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class AlternatingBackgroundItemDecoration(
    @ColorInt private val oddBackground: Int,
    @ColorInt private val evenBackground: Int
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        view.setBackgroundColor(
            if (position % 2 == 0) evenBackground
            else oddBackground
        )
    }
}
