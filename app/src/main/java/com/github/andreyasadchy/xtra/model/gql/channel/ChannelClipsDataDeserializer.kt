package com.github.andreyasadchy.xtra.model.gql.channel

import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ChannelClipsDataDeserializer : JsonDeserializer<ChannelClipsDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ChannelClipsDataResponse {
        val data = mutableListOf<Clip>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("user").getAsJsonObject("clips").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Clip(
                    id = if (!(obj.get("slug").isJsonNull)) { obj.getAsJsonPrimitive("slug").asString } else "",
                    broadcaster_id = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("id").asString } else null,
                    broadcaster_login = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("login").asString } else null,
                    broadcaster_name = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("displayName").asString } else null,
                    game_name = if (!(obj.get("game").isJsonNull)) { obj.getAsJsonObject("game").getAsJsonPrimitive("name").asString } else null,
                    title = if (!(obj.get("title").isJsonNull)) { obj.getAsJsonPrimitive("title").asString } else null,
                    view_count = if (!(obj.get("viewCount").isJsonNull)) { obj.getAsJsonPrimitive("viewCount").asInt } else null,
                    created_at = if (!(obj.get("createdAt").isJsonNull)) { obj.getAsJsonPrimitive("createdAt").asString } else null,
                    thumbnail_url = if (!(obj.get("thumbnailURL").isJsonNull)) { obj.getAsJsonPrimitive("thumbnailURL").asString } else null,
                    duration = if (!(obj.get("durationSeconds").isJsonNull)) { obj.getAsJsonPrimitive("durationSeconds").asDouble } else null,
                    profileImageURL = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("profileImageURL").asString } else null
                )
            )
        }
        return ChannelClipsDataResponse(data, cursor)
    }
}
