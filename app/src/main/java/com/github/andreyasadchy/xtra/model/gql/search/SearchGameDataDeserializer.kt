package com.github.andreyasadchy.xtra.model.gql.search

import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class SearchGameDataDeserializer : JsonDeserializer<SearchGameDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SearchGameDataResponse {
        val data = mutableListOf<Game>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("games").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("games").get("cursor").isJsonNull) json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("games").getAsJsonPrimitive("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("item")
            data.add(Game(
                id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                name = if (!(obj.get("displayName").isJsonNull)) { obj.getAsJsonPrimitive("displayName").asString } else null,
                box_art_url = if (!(obj.get("boxArtURL").isJsonNull)) { obj.getAsJsonPrimitive("boxArtURL").asString } else null,
                viewersCount = if (!(obj.get("viewersCount").isJsonNull)) { obj.getAsJsonPrimitive("viewersCount").asInt } else 0,
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
        return SearchGameDataResponse(data, cursor)
    }
}
