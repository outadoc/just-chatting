package com.github.andreyasadchy.xtra.model.gql.followed

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FollowingUserDataDeserializer : JsonDeserializer<FollowingUserDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FollowingUserDataResponse {
        val following = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("user").getAsJsonObject("self").get("follower")
        return FollowingUserDataResponse(!following.isJsonNull)
    }
}
