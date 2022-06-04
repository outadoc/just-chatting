package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class BttvGlobalDeserializer : JsonDeserializer<BttvGlobalResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BttvGlobalResponse {
        val emotes = mutableListOf<BttvEmote>()
        for (i in 0 until json.asJsonArray.size()) {
            val emote = json.asJsonArray.get(i).asJsonObject
            emotes.add(
                BttvEmote(
                    emote.get("id").asString,
                    emote.get("code").asString,
                    emote.get("imageType").asString
                )
            )
        }
        return BttvGlobalResponse(emotes)
    }
}
