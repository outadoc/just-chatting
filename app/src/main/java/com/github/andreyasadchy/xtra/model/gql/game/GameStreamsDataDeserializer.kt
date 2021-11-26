package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.stream.Stream
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
            cursor = it.asJsonObject.getAsJsonPrimitive("cursor").asString
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Stream(
                    obj.getAsJsonPrimitive("id").asString,
                    obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("id").asString,
                    obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("login").asString,
                    obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("displayName").asString,
                    game_name = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("game").getAsJsonPrimitive("displayName").asString,
                    type = obj.getAsJsonPrimitive("type").asString,
                    title = obj.getAsJsonPrimitive("title").asString,
                    viewer_count = obj.getAsJsonPrimitive("viewersCount").asInt,
                    thumbnail_url = obj.getAsJsonPrimitive("previewImageURL").asString,
                    profileImageURL = obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("profileImageURL").asString
                )
            )
        }
        return GameStreamsDataResponse(data, cursor)
    }
}
