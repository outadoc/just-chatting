package fr.outadoc.justchatting.ui.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

open class DefaultViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView), LayoutContainer
