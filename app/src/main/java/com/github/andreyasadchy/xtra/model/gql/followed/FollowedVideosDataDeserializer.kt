package com.github.andreyasadchy.xtra.model.gql.followed

import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FollowedVideosDataDeserializer : JsonDeserializer<FollowedVideosDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FollowedVideosDataResponse {
        val data = mutableListOf<Video>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("currentUser").getAsJsonObject("followedVideos").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Video(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else "",
                    user_id = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("id").asString } else null,
                    user_login = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("login").asString } else null,
                    user_name = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("displayName").asString } else null,
                    title = if (!(obj.get("title").isJsonNull)) { obj.getAsJsonPrimitive("title").asString } else null,
                    createdAt = if (!(obj.get("publishedAt").isJsonNull)) { obj.getAsJsonPrimitive("publishedAt").asString } else null,
                    thumbnail_url = if (!(obj.get("previewThumbnailURL").isJsonNull)) { obj.getAsJsonPrimitive("previewThumbnailURL").asString } else null,
                    view_count = if (!(obj.get("viewCount").isJsonNull)) { obj.getAsJsonPrimitive("viewCount").asInt } else null,
                    duration = if (!(obj.get("lengthSeconds").isJsonNull)) { obj.getAsJsonPrimitive("lengthSeconds").asString } else null,
                    gameId = if (!(obj.get("game").isJsonNull)) { obj.getAsJsonObject("game").getAsJsonPrimitive("id").asString } else null,
                    gameName = if (!(obj.get("game").isJsonNull)) { obj.getAsJsonObject("game").getAsJsonPrimitive("displayName").asString } else null,
                    profileImageURL = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("profileImageURL").asString } else null
                )
            )
        }
        return FollowedVideosDataResponse(data, cursor)
    }
}
