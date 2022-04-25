package com.github.andreyasadchy.xtra.model.gql.clip

data class ClipUrlsResponse(val videos: List<Video>) {

    data class Video(
            val frameRate: Int,
            val quality: String,
            val url: String)
}