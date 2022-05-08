package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
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
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Game(
                id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                name = if (!(obj.get("displayName").isJsonNull)) { obj.getAsJsonPrimitive("displayName").asString } else null,
                box_art_url = if (!(obj.get("avatarURL").isJsonNull)) { obj.getAsJsonPrimitive("avatarURL").asString } else null,
                viewersCount = if (!(obj.get("viewersCount").isJsonNull)) { obj.getAsJsonPrimitive("viewersCount").asInt } else null,
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
        return GameDataResponse(data, cursor)
    }
}
