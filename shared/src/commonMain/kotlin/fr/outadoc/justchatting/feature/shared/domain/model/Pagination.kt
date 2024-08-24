package fr.outadoc.justchatting.feature.shared.domain.model

internal sealed class Pagination {
    data class Next(val cursor: String) : Pagination()
}
