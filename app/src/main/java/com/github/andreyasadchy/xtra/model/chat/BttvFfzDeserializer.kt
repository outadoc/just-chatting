package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class BttvFfzDeserializer : JsonDeserializer<BttvFfzResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BttvFfzResponse {
        val emotes = mutableListOf<FfzEmote>()
        for (i in 0 until json.asJsonArray.size()) {
            val emote = json.asJsonArray.get(i).asJsonObject
            val urls = emote.getAsJsonObject("images")
            val url = urls.get(when (emoteQuality) {"4" -> ("4x") "3" -> ("2x") "2" -> ("2x") else -> ("1x")}).takeUnless { it?.isJsonNull == true }?.asString ?: urls.get("2x").takeUnless { it?.isJsonNull == true }?.asString ?: urls.get("1x").asString
            emotes.add(FfzEmote(emote.get("code").asString, url, "image/" + emote.get("imageType").asString))
        }
        return BttvFfzResponse(emotes)
    }
}
