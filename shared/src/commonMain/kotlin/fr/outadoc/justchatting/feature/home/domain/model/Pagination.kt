package fr.outadoc.justchatting.feature.home.domain.model

internal sealed class Pagination {
    data class Next(val cursor: String) : Pagination()
}
