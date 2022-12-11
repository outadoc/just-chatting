package fr.outadoc.justchatting.component.twitch.model.helix.stream

class StreamsResponse(
    val data: List<Stream>?,
    val pagination: Pagination?
)

data class Pagination(
    val cursor: String?
)
