package com.github.andreyasadchy.xtra.model.gql.followed

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FollowingGameDataDeserializer : JsonDeserializer<FollowingGameDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FollowingGameDataResponse {
        val following = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("game").getAsJsonObject("self").get("follow")
        return FollowingGameDataResponse(!following.isJsonNull)
    }
}
