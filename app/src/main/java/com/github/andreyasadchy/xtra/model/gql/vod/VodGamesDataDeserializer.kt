package com.github.andreyasadchy.xtra.model.gql.vod

import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class VodGamesDataDeserializer : JsonDeserializer<VodGamesDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): VodGamesDataResponse {
        val data = mutableListOf<Game>()
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("video").getAsJsonObject("moments").getAsJsonArray("edges")
        dataJson.forEach {
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Game(
                    id = if (!(obj.get("details").isJsonNull)) { obj.getAsJsonObject("details").getAsJsonObject("game").getAsJsonPrimitive("id").asString } else null,
                    name = if (!(obj.get("details").isJsonNull)) { obj.getAsJsonObject("details").getAsJsonObject("game").getAsJsonPrimitive("displayName").asString } else null,
                    box_art_url = if (!(obj.get("details").isJsonNull)) { obj.getAsJsonObject("details").getAsJsonObject("game").getAsJsonPrimitive("boxArtURL").asString } else null,
                    vodPosition = if (!(obj.get("positionMilliseconds").isJsonNull)) { obj.getAsJsonPrimitive("positionMilliseconds").asInt } else null,
                    vodDuration = if (!(obj.get("durationMilliseconds").isJsonNull)) { obj.getAsJsonPrimitive("durationMilliseconds").asInt } else null,
                )
            )
        }
        return VodGamesDataResponse(data)
    }
}
