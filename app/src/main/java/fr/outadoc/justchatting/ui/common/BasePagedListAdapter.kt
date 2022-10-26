package fr.outadoc.justchatting.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.repository.LoadingState

abstract class BasePagedListAdapter<T : Any>(diffCallback: DiffUtil.ItemCallback<T>) :
    PagingDataAdapter<T, DefaultViewHolder>(diffCallback) {

    protected abstract val layoutId: Int
    private var pagingState: LoadingState? = null

    protected abstract fun bind(item: T, view: View)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        return DefaultViewHolder(
            LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        if (getItemViewType(position) == layoutId) {
            bind(getItem(position)!!, holder.containerView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!hasExtraRow() || position != itemCount - 1) {
            layoutId
        } else {
            R.layout.paging_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setPagingState(pagingState: LoadingState) {
        val previousState = this.pagingState
        val hadExtraRow = hasExtraRow()
        this.pagingState = pagingState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != pagingState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    private fun hasExtraRow() = pagingState != null && pagingState != LoadingState.LOADED
}
