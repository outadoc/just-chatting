package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.feature.irc.ChatMessageParser
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class RecentMessagesDeserializer : JsonDeserializer<RecentMessagesResponse> {

    private val parser = ChatMessageParser()

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): RecentMessagesResponse {
        return RecentMessagesResponse(
            messages = json.asJsonObject
                .getAsJsonArray("messages")
                .mapNotNull { message -> message.asString.takeIf { !it.isNullOrBlank() } }
                .mapNotNull { message -> parser.parse(message) }
        )
    }
}
