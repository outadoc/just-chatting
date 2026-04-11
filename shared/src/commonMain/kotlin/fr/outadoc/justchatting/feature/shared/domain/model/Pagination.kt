package fr.outadoc.justchatting.feature.shared.domain.model

public sealed class Pagination {
    public data class Next(
        val cursor: String,
    ) : Pagination()
}
