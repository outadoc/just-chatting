package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.annotations.SerializedName

data class TwitchBadge(
    @SerializedName("image_url_1x")
    val imageUrl1x: String,
    @SerializedName("image_url_2x")
    val imageUrl2x: String,
    @SerializedName("image_url_4x")
    val imageUrl4x: String,
    val description: String,
    val title: String,
    @SerializedName("click_url")
    val clickUrl: String)
