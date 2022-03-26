package com.github.andreyasadchy.xtra.model.gql.followed

import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FollowedStreamsDataDeserializer : JsonDeserializer<FollowedStreamsDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FollowedStreamsDataResponse {
        val data = mutableListOf<Stream>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("currentUser").getAsJsonObject("followedLiveUsers").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Stream(
                id = if (!(obj.get("stream").isJsonNull)) { obj.getAsJsonObject("stream").getAsJsonPrimitive("id").asString } else null,
                user_id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                user_login = if (!(obj.get("login").isJsonNull)) { obj.getAsJsonPrimitive("login").asString } else null,
                user_name = if (!(obj.get("displayName").isJsonNull)) { obj.getAsJsonPrimitive("displayName").asString } else null,
                game_id = if (!(obj.get("stream").isJsonNull)) { obj.getAsJsonObject("stream").getAsJsonObject("game").getAsJsonPrimitive("id").asString } else null,
                game_name = if (!(obj.get("stream").isJsonNull)) { obj.getAsJsonObject("stream").getAsJsonObject("game").getAsJsonPrimitive("displayName").asString } else null,
                type = if (!(obj.get("stream").isJsonNull)) { obj.getAsJsonObject("stream").getAsJsonPrimitive("type").asString } else null,
                title = if (!(obj.get("stream").isJsonNull)) { obj.getAsJsonObject("stream").getAsJsonPrimitive("title").asString } else null,
                viewer_count = if (!(obj.get("stream").isJsonNull)) { obj.getAsJsonObject("stream").getAsJsonPrimitive("viewersCount").asInt } else 0,
                thumbnail_url = if (!(obj.get("stream").isJsonNull)) { obj.getAsJsonObject("stream").getAsJsonPrimitive("previewImageURL").asString } else null,
                profileImageURL = if (!(obj.get("profileImageURL").isJsonNull)) { obj.getAsJsonPrimitive("profileImageURL").asString } else null,
                tags = if (!(obj.get("stream").isJsonNull)) {
                    val tags = mutableListOf<Tag>()
                    obj.getAsJsonObject("stream").getAsJsonArray("tags").forEach { tag ->
                        tags.add(Tag(
                            id = tag.asJsonObject.getAsJsonPrimitive("id").asString,
                            name = tag.asJsonObject.getAsJsonPrimitive("localizedName").asString
                        ))
                    }
                    tags.ifEmpty { null }
                } else null
            ))
        }
        return FollowedStreamsDataResponse(data, cursor)
    }
}
