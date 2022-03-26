package com.github.andreyasadchy.xtra.model.gql.followed

import com.github.andreyasadchy.xtra.model.helix.video.Video

data class FollowedVideosDataResponse(val data: List<Video>, val cursor: String?)