package com.github.andreyasadchy.xtra.ui.videos

import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.BookmarksRepository
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel

abstract class BaseVideosViewModel(
    private val playerRepository: PlayerRepository,
    private val bookmarksRepository: BookmarksRepository) : PagedListViewModel<Video>() {

    val positions = playerRepository.loadVideoPositions()
    val bookmarks = bookmarksRepository.loadBookmarksLiveData()
}