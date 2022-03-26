package com.github.andreyasadchy.xtra.model.gql.followed

import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FollowedChannelsDataDeserializer : JsonDeserializer<FollowedChannelsDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FollowedChannelsDataResponse {
        val data = mutableListOf<Follow>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("user").getAsJsonObject("follows").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Follow(
                to_id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                to_login = if (!(obj.get("login").isJsonNull)) { obj.getAsJsonPrimitive("login").asString } else null,
                to_name = if (!(obj.get("displayName").isJsonNull)) { obj.getAsJsonPrimitive("displayName").asString } else null,
                followed_at = if (!(obj.get("self").isJsonNull)) { obj.getAsJsonObject("self").getAsJsonObject("follower").getAsJsonPrimitive("followedAt").asString } else null,
                profileImageURL = if (!(obj.get("profileImageURL").isJsonNull)) { obj.getAsJsonPrimitive("profileImageURL").asString } else null,
            ))
        }
        return FollowedChannelsDataResponse(data, cursor)
    }
}
