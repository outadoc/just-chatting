package fr.outadoc.justchatting.component.twitch.model.helix.follows

class FollowResponse(
    val total: Int?,
    val data: List<Follow>?,
    val pagination: Pagination?
)

data class Pagination(
    val cursor: String?
)
