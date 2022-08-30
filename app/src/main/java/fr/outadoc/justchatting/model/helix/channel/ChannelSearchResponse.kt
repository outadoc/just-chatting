package fr.outadoc.justchatting.model.helix.channel

class ChannelSearchResponse(
    val data: List<ChannelSearch>?,
    val pagination: Pagination?
)

data class Pagination(
    val cursor: String?
)
