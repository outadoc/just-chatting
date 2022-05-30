package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class EmoteCardDeserializer : JsonDeserializer<EmoteCardResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EmoteCardResponse {
        var data: EmoteCard? = null
        val obj = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("emote")
        if (!obj.isJsonNull) {
            data = EmoteCard(
                id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                name = if (!(obj.get("token").isJsonNull)) { obj.getAsJsonPrimitive("token").asString } else null,
                type = if (!(obj.get("type").isJsonNull)) { obj.getAsJsonPrimitive("type").asString } else null,
                subTier = if (!(obj.get("subscriptionTier").isJsonNull)) { obj.getAsJsonPrimitive("subscriptionTier").asString } else null,
                bitThreshold = if (!(obj.get("bitsBadgeTierSummary").isJsonNull)) { obj.getAsJsonObject("bitsBadgeTierSummary").getAsJsonPrimitive("threshold").asInt } else null,
                channelId = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("id").asString } else null,
                channelLogin = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("login").asString } else null,
                channelName = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("displayName").asString } else null
            )
        }
        return EmoteCardResponse(data)
    }
}
