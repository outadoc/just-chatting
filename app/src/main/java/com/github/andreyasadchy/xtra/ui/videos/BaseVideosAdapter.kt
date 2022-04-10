package com.github.andreyasadchy.xtra.ui.videos

import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.model.offline.Bookmark
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
abstract class BaseVideosAdapter(diffCallback: DiffUtil.ItemCallback<Video>) : BasePagedListAdapter<Video>(diffCallback) {

    protected var positions: Map<Long, Long>? = null

    fun setVideoPositions(positions: Map<Long, Long>) {
        this.positions = positions
        if (!currentList.isNullOrEmpty()) {
            notifyDataSetChanged()
        }
    }

    protected var bookmarks: List<Bookmark>? = null

    fun setBookmarksList(list: List<Bookmark>) {
        this.bookmarks = list
    }
}