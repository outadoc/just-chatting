/*
 * Copyright © 2016, Sky Welch <license@bunnies.io>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package chat.willow.kale

import chat.willow.kale.core.message.*
import chat.willow.kale.irc.message.IrcMessageParser
import chat.willow.kale.irc.message.rfc1459.ModeMessage
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

interface IKale {

    val lines: Observer<String>
    val messages: Observable<KaleObservable<IrcMessage>>

    fun <T> observe(descriptor: KaleDescriptor<T>): Observable<KaleObservable<T>>

    fun <M : Any> serialise(message: M): IrcMessage?

    var parsingStateDelegate: IKaleParsingStateDelegate?
}

interface IKaleParsingStateDelegate {

    fun modeTakesAParameter(isAdding: Boolean, token: Char): Boolean
}

class Kale(private val router: IKaleRouter, private val metadataFactory: IKaleMetadataFactory) : IKale {
    override val lines = PublishSubject.create<String>()!!
    override val messages: Observable<KaleObservable<IrcMessage>>

    private val LOGGER = loggerFor<Kale>()

    override var parsingStateDelegate: IKaleParsingStateDelegate? = null
        set(value) {
            ModeMessage.parsingStateDelegate = value
        }

    init {
        messages = PublishSubject.create()

        lines
            .flatMap(this::process)
            .subscribe(messages)
    }

    override fun <T> observe(descriptor: KaleDescriptor<T>): Observable<KaleObservable<T>> {
        return messages
            .filter { descriptor.matcher(it.message) }
            .flatMap { process(it, descriptor.parser) }
    }

    private fun process(line: String): Observable<KaleObservable<IrcMessage>> {
        val ircMessage = IrcMessageParser.parse(line)
        if (ircMessage == null) {
            LOGGER.warn("failed to parse line to IrcMessage: $line")
            return Observable.empty()
        }

        val metadata = metadataFactory.construct(ircMessage)
        return Observable.just(KaleObservable(ircMessage, metadata))
    }

    private fun <T> process(kaleObservable: KaleObservable<IrcMessage>, parser: IMessageParser<T>): Observable<KaleObservable<T>> {
        val parsedMessage = parser.parse(kaleObservable.message)
        if (parsedMessage == null) {
            LOGGER.warn("failed to parse message to expected type: ${kaleObservable.message} $kaleObservable")
            return Observable.empty()
        }

        return Observable.just(KaleObservable(parsedMessage, kaleObservable.meta))
    }

    override fun <M : Any> serialise(message: M): IrcMessage? {
        @Suppress("UNCHECKED_CAST")
        val factory = router.serialiserFor(message::class.java) as? IMessageSerialiser<M>
        if (factory == null) {
            LOGGER.warn("failed to find factory for message serialisation: $message")
            return null
        }

        return factory.serialise(message)
    }
}
