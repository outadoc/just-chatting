/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.component.ircparser.core.message

interface IMessageParser<out T> {

    fun parse(message: IrcMessage): T?
}

interface IComponentsParser<out T> {

    fun parseFromComponents(components: IrcMessageComponents): T?
}

abstract class MessageParser<out T> : IMessageParser<T>, IComponentsParser<T> {

    override fun parse(message: IrcMessage): T? {
        val components = IrcMessageComponents(tags = message.tags, prefix = message.prefix, parameters = message.parameters)
        return parseFromComponents(components)
    }
}
