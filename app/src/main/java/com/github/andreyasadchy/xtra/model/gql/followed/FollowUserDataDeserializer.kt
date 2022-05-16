package com.github.andreyasadchy.xtra.model.gql.followed

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FollowUserDataDeserializer : JsonDeserializer<FollowUserDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FollowUserDataResponse {
        val error = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("followUser").get("error")
        return FollowUserDataResponse(if (!error.isJsonNull) error.asString else null)
    }
}
