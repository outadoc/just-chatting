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
     * Frames 2, 5 and 7 use a transparent color index; the rest don't. [FrameDecoder] used to
     * allocate its scratch bitmap from `codec.imageInfo`, which only reflects frame 0's alpha
     * type. Skia refused frames whose alpha type didn't match, and the failure got swallowed —
     * so those frames were silently never drawn.
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

        // draw() takes position modulo totalDuration, so the next play-through's position 0
        // must resolve back to frame 0.
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
     * Control fixture: every frame here uses the same alpha type, so it never hit the bug above.
     * Keep this passing so a future change can't quietly break a GIF that already works.
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
