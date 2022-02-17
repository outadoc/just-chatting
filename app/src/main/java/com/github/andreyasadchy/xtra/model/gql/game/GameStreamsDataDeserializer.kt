package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class GameStreamsDataDeserializer : JsonDeserializer<GameStreamsDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GameStreamsDataResponse {
        val data = mutableListOf<Stream>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("game").getAsJsonObject("streams").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Stream(
                id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                user_id = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("id").asString } else null,
                user_login = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("login").asString } else null,
                user_name = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("displayName").asString } else null,
                type = if (!(obj.get("type").isJsonNull)) { obj.getAsJsonPrimitive("type").asString } else null,
                title = if (!(obj.get("title").isJsonNull)) { obj.getAsJsonPrimitive("title").asString } else null,
                viewer_count = if (!(obj.get("viewersCount").isJsonNull)) { obj.getAsJsonPrimitive("viewersCount").asInt } else 0,
                thumbnail_url = if (!(obj.get("previewImageURL").isJsonNull)) { obj.getAsJsonPrimitive("previewImageURL").asString } else null,
                profileImageURL = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("profileImageURL").asString } else null,
                tags = if (!(obj.get("tags").isJsonNull)) {
                    val tags = mutableListOf<Tag>()
                    obj.getAsJsonArray("tags").forEach { tag ->
                        tags.add(Tag(
                            id = tag.asJsonObject.getAsJsonPrimitive("id").asString,
                            name = tag.asJsonObject.getAsJsonPrimitive("localizedName").asString
                        ))
                    }
                    tags.ifEmpty { null }
                } else null
            ))
        }
        return GameStreamsDataResponse(data, cursor)
    }
}
