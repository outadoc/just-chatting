package com.github.andreyasadchy.xtra.model.gql.channel

import com.github.andreyasadchy.xtra.model.helix.channel.ChannelViewerList
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ChannelViewerListDataDeserializer : JsonDeserializer<ChannelViewerListDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ChannelViewerListDataResponse {
        val broadcasters = mutableListOf<String>()
        val moderators = mutableListOf<String>()
        val vips = mutableListOf<String>()
        val viewers = mutableListOf<String>()
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("channel").getAsJsonObject("chatters")
        dataJson.getAsJsonArray("broadcasters").forEach {
            val obj = it.asJsonObject
            if (!(obj.get("login").isJsonNull)) { broadcasters.add(obj.getAsJsonPrimitive("login").asString) }
        }
        dataJson.getAsJsonArray("moderators").forEach {
            val obj = it.asJsonObject
            if (!(obj.get("login").isJsonNull)) { moderators.add(obj.getAsJsonPrimitive("login").asString) }
        }
        dataJson.getAsJsonArray("vips").forEach {
            val obj = it.asJsonObject
            if (!(obj.get("login").isJsonNull)) { vips.add(obj.getAsJsonPrimitive("login").asString) }
        }
        dataJson.getAsJsonArray("viewers").forEach {
            val obj = it.asJsonObject
            if (!(obj.get("login").isJsonNull)) { viewers.add(obj.getAsJsonPrimitive("login").asString) }
        }
        val count = if (!(dataJson.get("count").isJsonNull)) { dataJson.getAsJsonPrimitive("count").asInt } else null
        return ChannelViewerListDataResponse(ChannelViewerList(broadcasters, moderators, vips, viewers, count))
    }
}
