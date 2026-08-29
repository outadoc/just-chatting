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
            val result: DecodeResult = AnimatedSkiaImageDecoder(source = opcrotteRageImageSource()).decode()
            val image = result.image as AnimatedSkiaImage

            assertEquals(112, image.width)
            assertEquals(112, image.height)
            assertEquals(OPCROTTE_RAGE_FRAME_COUNT, image.frames.size)
        }

    /**
     * Regression test — **expected to fail** until the underlying bug is fixed.
     *
     * `opcrotteRage.gif` mixes fully-opaque frames with frames 2, 5 and 7, which use a
     * transparent color index (Skia reports their [org.jetbrains.skia.AnimationFrameInfo.alphaType]
     * as `UNPREMUL`). [FrameDecoder] allocates its reusable scratch bitmap from
     * `codec.imageInfo`, which reflects only frame 0's (`OPAQUE`) alpha type. Skia's
     * `Codec.readPixels` refuses to decode a frame whose native alpha type doesn't match that
     * bitmap, throwing `IllegalArgumentException`, which [AnimatedSkiaImage.decodeFrame] swallows
     * — so those three frames are silently never drawn, i.e. the animation appears to drop them.
     *
     * Fix: allocate `FrameDecoder`'s scratch bitmap from its own `imageInfo` field (already
     * `UNPREMUL`) instead of `codec.imageInfo`.
     */
    @Test
    fun `every frame of opcrotteRage gif decodes successfully`() {
        val diagnostics = diagnoseFrames(opcrotteRageCodec())
        val failed = diagnostics.filterNot { it.decoded }

        assertTrue(
            failed.isEmpty(),
            "Expected every frame to decode, but ${failed.size} frame(s) failed:${diagnostics.toReportString()}",
        )
    }

    @Test
    fun `frameIndexAt returns the frame active at a given offset`() {
        val codec = opcrotteRageCodec()
        val image = AnimatedSkiaImage(codec, prerenderFrames = false)

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

    private companion object {
        private const val OPCROTTE_RAGE_FRAME_COUNT = 9
        private const val OPCROTTE_RAGE_TOTAL_DURATION_MS = 540L
    }
}

private fun opcrotteRageBytes(): ByteArray =
    checkNotNull(
        AnimatedSkiaImageDecoderTest::class.java.getResourceAsStream("/gif/opcrotteRage.gif"),
    ) { "Test resource /gif/opcrotteRage.gif not found on classpath" }.use { it.readBytes() }

private fun opcrotteRageCodec(): Codec = Codec.makeFromData(Data.makeFromBytes(opcrotteRageBytes()))

private fun opcrotteRageImageSource(): ImageSource =
    ImageSource(
        source = Buffer().apply { write(opcrotteRageBytes()) },
        fileSystem = FileSystem.SYSTEM,
    )
