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
import chat.willow.kale.core.tag.IKaleTagRouter
import chat.willow.kale.core.tag.ITagStore
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KaleTests {

    private lateinit var sut: Kale
    private lateinit var router: IKaleRouter
    private lateinit var tagRouter: IKaleTagRouter
    private lateinit var metadataFactory: IKaleMetadataFactory
    private lateinit var metadata: ITagStore

    object TestMessageOne
    object TestMessageTwo

    private fun <T> testDescriptor(command: String, result: T?) = KaleDescriptor(
        matcher = { it.command == command },
        parser = object : MessageParser<T>() {
            override fun parseFromComponents(components: IrcMessageComponents): T? = result
        }
    )

    @Before fun setUp() {
        router = mock()
        tagRouter = mock()
        metadataFactory = mock()
        metadata = mock()

        whenever(metadataFactory.construct(any())).thenReturn(metadata)

        sut = Kale(router, metadataFactory)
    }

    @Test fun `when an unparseable line is given, nothing happens`() {
        val observer = sut.messages.test()

        sut.lines.onNext(" ")

        observer.assertNoValues()
    }

    @Test fun `when a correct line is given, and a valid observer is registered, it is notified`() {
        val testObserver = sut.observe(testDescriptor("something", result = TestMessageOne)).test()

        sut.lines.onNext("something")

        testObserver.assertValue(KaleObservable(TestMessageOne, metadata))
    }

    @Test fun `when multiple correct lines are given, and valid observers are registered, they are notified in order`() {
        val testObserverOne = sut.observe(testDescriptor("something1", result = TestMessageOne))
        val testObserverTwo = sut.observe(testDescriptor("something2", result = TestMessageTwo))

        val results = Observable.merge(testObserverOne, testObserverTwo).test()

        sut.lines.onNext("something1")
        sut.lines.onNext("something2")

        results.assertValues(KaleObservable(TestMessageOne, metadata), KaleObservable(TestMessageTwo, metadata))
    }

    @Test fun `when multiple misc lines are given, observers are still notified in order`() {
        val testObserverOne = sut.observe(testDescriptor("something1", result = TestMessageOne))
        val testObserverTwo = sut.observe(testDescriptor("something2", result = TestMessageTwo))

        val results = Observable.merge(testObserverOne, testObserverTwo).test()

        sut.lines.onNext("something2")
        sut.lines.onNext("not_something")
        sut.lines.onNext("something1")
        sut.lines.onNext("still_not_something")

        results.assertValues(KaleObservable(TestMessageTwo, metadata), KaleObservable(TestMessageOne, metadata))
    }

    @Test fun `when a parseable line is given, messages outputs correct line`() {
        val results = sut.messages.test()

        sut.lines.onNext("HELLO kale")

        results.assertValue(KaleObservable(IrcMessage(command = "HELLO", parameters = listOf("kale")), metadata))
    }

    @Test fun `when parseable lines are given, messages output in order`() {
        val results = sut.messages.test()

        sut.lines.onNext("HELLO kale")
        sut.lines.onNext("WORLD kale")

        results.assertValues(
            KaleObservable(IrcMessage(command = "HELLO", parameters = listOf("kale")), metadata),
            KaleObservable(IrcMessage(command = "WORLD", parameters = listOf("kale")), metadata)
        )
    }

    @Test fun `when misc lines are given, messages still output in order`() {
        val results = sut.messages.test()

        sut.lines.onNext(" ")
        sut.lines.onNext("HELLO kale")
        sut.lines.onNext(" ")
        sut.lines.onNext("WORLD kale")
        sut.lines.onNext(" ")

        results.assertValues(
            KaleObservable(IrcMessage(command = "HELLO", parameters = listOf("kale")), metadata),
            KaleObservable(IrcMessage(command = "WORLD", parameters = listOf("kale")), metadata)
        )
    }

    @Test fun `when serialising without a route, return null`() {
        val thing: Any = mock()

        val result = sut.serialise(thing)

        assertNull(result)
    }

    @Test fun `when serialising with a valid route, return correct message`() {
        val thing: Int = 1
        val expectedReturnMessage: IrcMessage = IrcMessage(command = "ANY")

        val anySerialiser = object : IMessageSerialiser<Int> {
            override fun serialise(message: Int): IrcMessage? {
                return expectedReturnMessage
            }
        }

        whenever(router.serialiserFor(any<Class<*>>())).thenReturn(anySerialiser)

        val result = sut.serialise(thing)

        assertTrue(expectedReturnMessage === result)
    }
}
