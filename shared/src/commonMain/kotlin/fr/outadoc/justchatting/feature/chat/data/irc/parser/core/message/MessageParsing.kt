/*
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.feature.chat.data.irc.parser.core.message

internal interface IMessageParser<out T> {
    fun parse(message: IrcMessage): T?
}

internal interface IComponentsParser<out T> {
    fun parseFromComponents(components: IrcMessageComponents): T?
}

internal abstract class MessageParser<out T> :
    IMessageParser<T>,
    IComponentsParser<T> {
    override fun parse(message: IrcMessage): T? {
        val components = IrcMessageComponents(tags = message.tags, prefix = message.prefix, parameters = message.parameters)
        return parseFromComponents(components)
    }
}
