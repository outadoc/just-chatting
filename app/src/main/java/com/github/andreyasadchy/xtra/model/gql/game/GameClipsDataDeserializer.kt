package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class GameClipsDataDeserializer : JsonDeserializer<GameClipsDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GameClipsDataResponse {
        val data = mutableListOf<Clip>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("game").getAsJsonObject("clips").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull)
                it.asJsonObject.getAsJsonPrimitive("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Clip(
                    id = obj.getAsJsonPrimitive("slug").asString,
                    broadcaster_id = obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("id").asString,
                    broadcaster_name = obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("displayName").asString,
                    game_id = obj.getAsJsonObject("game").getAsJsonPrimitive("name").asString,
                    title = obj.getAsJsonPrimitive("title").asString,
                    view_count = obj.getAsJsonPrimitive("viewCount").asInt,
                    created_at = obj.getAsJsonPrimitive("createdAt").asString,
                    thumbnail_url = obj.getAsJsonPrimitive("thumbnailURL").asString,
                    duration = obj.getAsJsonPrimitive("durationSeconds").asDouble,
                    profileImageURL = obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("profileImageURL").asString
                )
            )
        }
        return GameClipsDataResponse(data, cursor)
    }
}
