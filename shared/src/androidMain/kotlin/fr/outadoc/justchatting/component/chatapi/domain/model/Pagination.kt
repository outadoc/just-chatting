package fr.outadoc.justchatting.component.chatapi.domain.model

sealed class Pagination {
    data class Previous(val cursor: String) : Pagination()
    data class Next(val cursor: String) : Pagination()
}
