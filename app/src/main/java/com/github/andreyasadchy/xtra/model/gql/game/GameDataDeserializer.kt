package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class GameDataDeserializer : JsonDeserializer<GameDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GameDataResponse {
        val data = mutableListOf<Game>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("directoriesWithTags").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = it.asJsonObject.getAsJsonPrimitive("cursor").asString
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Game(
                obj.getAsJsonPrimitive("id").asString,
                obj.getAsJsonPrimitive("displayName").asString,
                obj.getAsJsonPrimitive("avatarURL").asString,
                obj.getAsJsonPrimitive("viewersCount").asInt)
            )
        }
        return GameDataResponse(data, cursor)
    }
}
