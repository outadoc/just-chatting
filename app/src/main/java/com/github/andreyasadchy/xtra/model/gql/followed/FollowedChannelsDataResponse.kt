package com.github.andreyasadchy.xtra.model.gql.followed

import com.github.andreyasadchy.xtra.model.helix.follows.Follow

data class FollowedChannelsDataResponse(val data: List<Follow>, val cursor: String?)