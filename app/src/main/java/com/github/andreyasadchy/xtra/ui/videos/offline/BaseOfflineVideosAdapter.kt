package com.github.andreyasadchy.xtra.ui.videos.offline

import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter

abstract class BaseOfflineVideosAdapter(diffCallback: DiffUtil.ItemCallback<OfflineVideo>) : BasePagedListAdapter<OfflineVideo>(diffCallback) {

    protected var positions: Map<Long, Long>? = null

    fun setVideoPositions(positions: Map<Long, Long>) {
        this.positions = positions
        if (!currentList.isNullOrEmpty()) {
            notifyDataSetChanged()
        }
    }
}