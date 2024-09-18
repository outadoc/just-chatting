package fr.outadoc.justchatting.feature.timeline.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Video(
    @SerialName("id")
    val id: String,
    @SerialName("stream_id")
    val streamId: String?,
    @SerialName("user_id")
    val userId: String,
    @SerialName("user_login")
    val userLogin: String,
    @SerialName("user_name")
    val userDisplayName: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("created_at")
    val createdAtIso: String,
    @SerialName("published_at")
    val publishedAtIso: String,
    @SerialName("url")
    val videoUrl: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String,
    @SerialName("view_count")
    val viewCount: Int,
    @SerialName("duration")
    val duration: String,
)
