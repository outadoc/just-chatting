package com.github.andreyasadchy.xtra.model.gql.followed

import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FollowedGamesDataDeserializer : JsonDeserializer<FollowedGamesDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FollowedGamesDataResponse {
        val data = mutableListOf<Game>()
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("currentUser").getAsJsonObject("followedGames").getAsJsonArray("nodes")
        dataJson.forEach {
            val obj = it.asJsonObject
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
                } else null,
            ))
        }
        return FollowedGamesDataResponse(data)
    }
}
