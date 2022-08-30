package fr.outadoc.justchatting.model.helix.follows

class FollowResponse(
    val total: Int?,
    val data: List<Follow>?,
    val pagination: Pagination?
)

data class Pagination(
    val cursor: String?
)
