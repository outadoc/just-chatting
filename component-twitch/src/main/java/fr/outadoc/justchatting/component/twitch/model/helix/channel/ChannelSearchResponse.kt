package fr.outadoc.justchatting.component.twitch.model.helix.channel

class ChannelSearchResponse(
    val data: List<ChannelSearch>?,
    val pagination: Pagination?
)

data class Pagination(
    val cursor: String?
)
