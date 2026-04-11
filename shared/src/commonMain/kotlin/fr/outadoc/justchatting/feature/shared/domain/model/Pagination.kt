package fr.outadoc.justchatting.feature.shared.domain.model

internal sealed class Pagination {
    public data class Next(
        val cursor: String,
    ) : Pagination()
}
