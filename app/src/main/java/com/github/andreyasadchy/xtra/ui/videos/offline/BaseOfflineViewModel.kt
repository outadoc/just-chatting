package com.github.andreyasadchy.xtra.ui.videos.offline

import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel

abstract class BaseOfflineViewModel(private val playerRepository: PlayerRepository) : PagedListViewModel<OfflineVideo>() {

    val positions = playerRepository.loadVideoPositions()
}