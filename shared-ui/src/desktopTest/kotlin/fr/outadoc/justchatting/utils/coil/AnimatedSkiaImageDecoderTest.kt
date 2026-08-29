package fr.outadoc.justchatting.utils.coil

import coil3.decode.DecodeResult
import coil3.decode.ImageSource
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.FileSystem
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnimatedSkiaImageDecoderTest {
    @Test
    fun `decodes opcrotteRage gif dimensions and frame count`() =
        runTest {
            val result: DecodeResult = AnimatedSkiaImageDecoder(source = gifImageSource(OPCROTTE_RAGE)).decode()
            val image = result.image as AnimatedSkiaImage

            assertEquals(112, image.width)
            assertEquals(112, image.height)
            assertEquals(OPCROTTE_RAGE_FRAME_COUNT, image.frames.size)
        }

    /**
     * Regression test for the dropped-frames bug: `opcrotteRage.gif` mixes fully-opaque frames
     * with frames 2, 5 and 7, which use a transparent color index (Skia reports their
     * [org.jetbrains.skia.AnimationFrameInfo.alphaType] as `UNPREMUL`). [FrameDecoder] used to
     * allocate its reusable scratch bitmap from `codec.imageInfo`, which reflects only frame 0's
     * (`OPAQUE`) alpha type — Skia's `Codec.readPixels` refuses to decode a frame whose native
     * alpha type doesn't match that bitmap, throwing `IllegalArgumentException`, which
     * [AnimatedSkiaImage.decodeFrame] swallows, so those three frames were silently never drawn.
     * Fixed by allocating the scratch bitmap from `FrameDecoder`'s own `imageInfo` field (already
     * `UNPREMUL`) instead.
     */
    @Test
    fun `every frame of opcrotteRage gif decodes successfully`() {
        val diagnostics = diagnoseFrames(gifCodec(OPCROTTE_RAGE))
        val failed = diagnostics.filterNot { it.decoded }

        assertTrue(
            failed.isEmpty(),
            "Expected every frame to decode, but ${failed.size} frame(s) failed:${diagnostics.toReportString()}",
        )
    }

    @Test
    fun `frameIndexAt returns the frame active at a given offset for opcrotteRage gif`() {
        val image = AnimatedSkiaImage(gifCodec(OPCROTTE_RAGE), prerenderFrames = false)

        // opcrotteRage.gif: 9 frames, 60ms each, looping forever (total play-through = 540ms).
        assertEquals(0, image.frameIndexAt(positionInPlayThrough = 0))
        assertEquals(0, image.frameIndexAt(positionInPlayThrough = 59))
        assertEquals(1, image.frameIndexAt(positionInPlayThrough = 60))
        assertEquals(1, image.frameIndexAt(positionInPlayThrough = 119))
        assertEquals(2, image.frameIndexAt(positionInPlayThrough = 120))
        assertEquals(8, image.frameIndexAt(positionInPlayThrough = 539))

        // Positions are taken modulo totalDuration by draw(), so 540 itself is never passed in
        // practice, but the wraparound is what a caller relies on: position 0 of the next
        // play-through must resolve back to frame 0.
        assertEquals(0, image.frameIndexAt(positionInPlayThrough = 540 % OPCROTTE_RAGE_TOTAL_DURATION_MS))
    }

    @Test
    fun `decodes angledDance gif dimensions and frame count`() =
        runTest {
            val result: DecodeResult = AnimatedSkiaImageDecoder(source = gifImageSource(ANGLED_DANCE)).decode()
            val image = result.image as AnimatedSkiaImage

            assertEquals(112, image.width)
            assertEquals(112, image.height)
            assertEquals(ANGLED_DANCE_FRAME_COUNT, image.frames.size)
        }

    /**
     * Known-good control fixture, contrasting with `opcrotteRage.gif` above: all 16 frames use
     * the *same* transparent-color-index alpha type (`UNPREMUL`), unlike `opcrotteRage.gif`,
     * which mixes opaque and transparent frames. `codec.imageInfo`'s aggregate alpha type
     * therefore matches every individual frame here, so none of them hit the `FrameDecoder`
     * allocation mismatch. Keeping this test passing guards against a future fix for the
     * `opcrotteRage.gif` regression accidentally breaking a GIF that already decodes correctly.
     */
    @Test
    fun `every frame of angledDance gif decodes successfully`() {
        val diagnostics = diagnoseFrames(gifCodec(ANGLED_DANCE))
        val failed = diagnostics.filterNot { it.decoded }

        assertTrue(
            failed.isEmpty(),
            "Expected every frame to decode, but ${failed.size} frame(s) failed:${diagnostics.toReportString()}",
        )
    }

    @Test
    fun `frameIndexAt returns the frame active at a given offset for angledDance gif`() {
        val image = AnimatedSkiaImage(gifCodec(ANGLED_DANCE), prerenderFrames = false)

        // angledDance.gif: 16 frames, 40ms each, looping forever (total play-through = 640ms).
        assertEquals(0, image.frameIndexAt(positionInPlayThrough = 0))
        assertEquals(0, image.frameIndexAt(positionInPlayThrough = 39))
        assertEquals(1, image.frameIndexAt(positionInPlayThrough = 40))
        assertEquals(15, image.frameIndexAt(positionInPlayThrough = 639))

        assertEquals(0, image.frameIndexAt(positionInPlayThrough = 640 % ANGLED_DANCE_TOTAL_DURATION_MS))
    }

    private companion object {
        private const val OPCROTTE_RAGE = "opcrotteRage.gif"
        private const val OPCROTTE_RAGE_FRAME_COUNT = 9
        private const val OPCROTTE_RAGE_TOTAL_DURATION_MS = 540L

        private const val ANGLED_DANCE = "angledDance.gif"
        private const val ANGLED_DANCE_FRAME_COUNT = 16
        private const val ANGLED_DANCE_TOTAL_DURATION_MS = 640L
    }
}

private fun gifBytes(fileName: String): ByteArray =
    checkNotNull(
        AnimatedSkiaImageDecoderTest::class.java.getResourceAsStream("/gif/$fileName"),
    ) { "Test resource /gif/$fileName not found on classpath" }.use { it.readBytes() }

private fun gifCodec(fileName: String): Codec = Codec.makeFromData(Data.makeFromBytes(gifBytes(fileName)))

private fun gifImageSource(fileName: String): ImageSource =
    ImageSource(
        source = Buffer().apply { write(gifBytes(fileName)) },
        fileSystem = FileSystem.SYSTEM,
    )
