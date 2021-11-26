package com.github.andreyasadchy.xtra.model.gql.search

import com.github.andreyasadchy.xtra.model.helix.channel.Channel
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class SearchChannelDataDeserializer : JsonDeserializer<SearchChannelDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SearchChannelDataResponse {
        val data = mutableListOf<Channel>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("channels").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("channels").getAsJsonPrimitive("cursor").asString
            val obj = it.asJsonObject.getAsJsonObject("item")
            data.add(Channel(
                    obj.getAsJsonPrimitive("id").asString,
                    obj.getAsJsonPrimitive("login").asString,
                    obj.getAsJsonPrimitive("displayName").asString,
                    thumbnail_url = obj.getAsJsonPrimitive("profileImageURL").asString
                )
            )
        }
        return SearchChannelDataResponse(data, cursor)
    }
}
