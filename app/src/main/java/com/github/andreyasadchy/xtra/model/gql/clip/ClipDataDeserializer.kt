package com.github.andreyasadchy.xtra.model.gql.clip

import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ClipDataDeserializer : JsonDeserializer<ClipDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ClipDataResponse {
        val obj = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("clip")
        val data = (Clip(
            id = "",
            broadcaster_id = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("id").asString } else null,
            broadcaster_login = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("login").asString } else null,
            broadcaster_name = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("displayName").asString } else null,
            profileImageURL = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("profileImageURL").asString } else null,
            videoOffsetSeconds = if (!(obj.get("videoOffsetSeconds").isJsonNull)) { obj.getAsJsonPrimitive("videoOffsetSeconds").asInt } else null,
        ))
        return ClipDataResponse(data)
    }
}
