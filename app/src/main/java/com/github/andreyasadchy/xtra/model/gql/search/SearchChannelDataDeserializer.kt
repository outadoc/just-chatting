package com.github.andreyasadchy.xtra.model.gql.search

import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class SearchChannelDataDeserializer : JsonDeserializer<SearchChannelDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SearchChannelDataResponse {
        val data = mutableListOf<ChannelSearch>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("channels").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("channels").get("cursor").isJsonNull) json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("channels").getAsJsonPrimitive("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("item")
            data.add(ChannelSearch(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                    broadcaster_login = if (!(obj.get("login").isJsonNull)) { obj.getAsJsonPrimitive("login").asString } else null,
                    display_name = if (!(obj.get("displayName").isJsonNull)) { obj.getAsJsonPrimitive("displayName").asString } else null,
                    profileImageURL = if (!(obj.get("profileImageURL").isJsonNull)) { obj.getAsJsonPrimitive("profileImageURL").asString } else null,
                    followers_count = if (!(obj.get("followers").isJsonNull)) { obj.getAsJsonObject("followers").getAsJsonPrimitive("totalCount").asInt } else null,
                )
            )
        }
        return SearchChannelDataResponse(data, cursor)
    }
}
