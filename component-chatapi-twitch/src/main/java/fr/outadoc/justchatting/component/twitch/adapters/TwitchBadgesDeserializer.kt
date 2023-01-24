package fr.outadoc.justchatting.component.twitch.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.component.twitch.model.TwitchBadge
import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse
import fr.outadoc.justchatting.utils.core.asStringOrNull
import kotlinx.collections.immutable.toImmutableMap
import java.lang.reflect.Type

class TwitchBadgesDeserializer : JsonDeserializer<TwitchBadgesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TwitchBadgesResponse {
        val badges = json.asJsonObject
            .getAsJsonObject("badge_sets")
            .entrySet()
            .flatMap { set ->
                set.value.asJsonObject
                    .getAsJsonObject("versions")
                    .entrySet().map { version ->
                        val urls = version.value.asJsonObject
                        TwitchBadge(
                            id = set.key,
                            version = version.key,
                            urls = listOf(
                                1f to urls.get("image_url_1x").asStringOrNull,
                                2f to urls.get("image_url_2x").asStringOrNull,
                                4f to urls.get("image_url_4x").asStringOrNull
                            )
                                .mapNotNull { (density, url) ->
                                    if (url != null) density to url else null
                                }
                                .toMap()
                                .toImmutableMap()
                        )
                    }
            }

        return TwitchBadgesResponse(badges)
    }
}