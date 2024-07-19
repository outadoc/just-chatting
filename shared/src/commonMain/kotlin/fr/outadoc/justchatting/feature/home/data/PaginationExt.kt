package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource.LoadResult
import fr.outadoc.justchatting.feature.home.data.model.Pagination

internal val Pagination.itemsAfter: Int
    get() = (if (cursor == null) 0 else LoadResult.Page.COUNT_UNDEFINED)

internal val Pagination.nextKey: fr.outadoc.justchatting.feature.home.domain.model.Pagination.Next?
    get() = cursor?.let { cursor ->
        fr.outadoc.justchatting.feature.home.domain.model.Pagination.Next(
            cursor,
        )
    }
