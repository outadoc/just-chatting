package fr.outadoc.justchatting.component.twitch.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.component.twitch.model.chat.CheerEmote
import fr.outadoc.justchatting.component.twitch.model.chat.CheerEmotesResponse
import java.lang.reflect.Type

class CheerEmotesDeserializer : JsonDeserializer<CheerEmotesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CheerEmotesResponse {
        val emotes = json.asJsonObject.get("data")
            .asJsonArray
            .map { it.asJsonObject }
            .flatMap { emote ->
                val name = emote.get("prefix").asString
                emote.get("tiers").asJsonArray
                    .map { tier ->
                        val minBits = tier.asJsonObject.get("min_bits")
                        val color = tier.asJsonObject.get("color")
                        val images = tier.asJsonObject.get("images").asJsonObject

                        val darkImages = images
                            .get("dark")
                            .takeUnless { it.isJsonNull }
                            ?.asJsonObject

                        val lightImages = images
                            .get("dark")
                            .takeUnless { it.isJsonNull }
                            ?.asJsonObject

                        CheerEmote(
                            name = name + minBits.asInt,
                            minBits = minBits.asInt,
                            color = color.asString,
                            images = darkImages?.getImages("dark").orEmpty() +
                                lightImages?.getImages("light").orEmpty()
                        )
                    }
            }

        return CheerEmotesResponse(emotes)
    }

    private fun JsonObject.getImages(theme: String): List<CheerEmote.Image> {
        val staticUrls = get("static")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject

        val animatedUrls = get("animated")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject

        return staticUrls?.toMap("static", theme).orEmpty() +
            animatedUrls?.toMap("animated", theme).orEmpty()
    }

    private fun JsonObject.toMap(animation: String, theme: String): List<CheerEmote.Image> {
        return keySet().map { key ->
            CheerEmote.Image(
                theme = theme,
                isAnimated = animation == "animated",
                dpiScale = key.toFloat(),
                url = get(key).asString
            )
        }
    }
}
