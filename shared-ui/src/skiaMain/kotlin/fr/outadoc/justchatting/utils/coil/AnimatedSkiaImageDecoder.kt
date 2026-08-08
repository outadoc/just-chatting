package fr.outadoc.justchatting.utils.coil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import coil3.Canvas
import coil3.Image
import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.transformations
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import okio.use
import org.jetbrains.skia.AnimationDisposalMode
import org.jetbrains.skia.AnimationFrameInfo
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Data
import org.jetbrains.skia.ImageInfo
import kotlin.experimental.and
import kotlin.time.TimeSource
import org.jetbrains.skia.Image as SkiaImage

@Deprecated("Replace with proper coil3 implementation once available")
internal class AnimatedSkiaImageDecoder(
    private val source: ImageSource,
    private val prerenderFrames: Boolean = true,
) : Decoder {
    override suspend fun decode(): DecodeResult {
        val bytes = source.source().use { it.readByteArray() }
        val codec = Codec.makeFromData(Data.makeFromBytes(bytes))
        return DecodeResult(
            image = AnimatedSkiaImage(codec, prerenderFrames),
            isSampled = false,
        )
    }

    class Factory(
        private val prerenderFrames: Boolean = false,
    ) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            // Coil can only apply transformations to a BitmapImage, and asking it to convert one
            // of ours fails outright on non-Android platforms. Leave these requests to the default
            // decoder, which will produce a static image of the first frame.
            if (options.transformations.isNotEmpty()) {
                return null
            }

            if (!isGif(result.source.source()) && !isAnimatedWebP(result.source.source())) {
                return null
            }

            return AnimatedSkiaImageDecoder(
                source = result.source,
                prerenderFrames = prerenderFrames,
            )
        }
    }
}

private class AnimatedSkiaImage(
    codec: Codec,
    prerenderFrames: Boolean,
) : Image {
    override val width: Int = codec.width
    override val height: Int = codec.height

    override val shareable: Boolean
        get() = false

    private val imageInfo =
        ImageInfo(
            colorInfo =
            ColorInfo(
                colorType = ColorType.BGRA_8888,
                alphaType = ColorAlphaType.UNPREMUL,
                colorSpace = ColorSpace.sRGB,
            ),
            width = width,
            height = height,
        )

    // Each of these is a native call that reallocates its result on every access, so read them
    // once here rather than from within draw().
    private val frameCount: Int = codec.frameCount
    private val repetitionCount: Int = codec.repetitionCount

    private val frameDurations: IntArray

    /**
     * Whether frame `index - 1` may be handed to Skia as the starting point for decoding frame
     * `index`. Skia rejects a prior frame that was disposed with
     * [AnimationDisposalMode.RESTORE_PREVIOUS], as it doesn't contribute to the next frame.
     */
    private val canBlendOverPreviousFrame: BooleanArray

    init {
        // Reading this rebuilds the whole array natively, so read it once and keep only the two
        // things we actually need from it afterwards.
        val framesInfo: Array<AnimationFrameInfo> = codec.framesInfo

        frameDurations =
            IntArray(frameCount) { index ->
                framesInfo.getOrNull(index)?.safeFrameDuration ?: DEFAULT_FRAME_DURATION
            }

        canBlendOverPreviousFrame =
            BooleanArray(frameCount) { index ->
                val previousFrame = framesInfo.getOrNull(index - 1)
                previousFrame != null &&
                    previousFrame.disposalMethod != AnimationDisposalMode.RESTORE_PREVIOUS
            }
    }

    /**
     * Decodes frames on demand. Released as soon as there is nothing left to decode, as it holds
     * on to native memory that would otherwise stay alive for as long as the image does.
     */
    private var frameDecoder: FrameDecoder? = FrameDecoder(codec, imageInfo)

    private val frames: Array<SkiaImage?> =
        Array(frameCount) { index ->
            if (prerenderFrames) decodeFrame(index) else null
        }

    init {
        if (prerenderFrames) {
            releaseFrameDecoder()
        }
    }

    private var invalidateTick by mutableIntStateOf(0)

    private var currentRepetitionStartTime: TimeSource.Monotonic.ValueTimeMark? = null
    private var currentRepetitionCount = 0
    private var lastDrawnFrameIndex = 0
    private var isAnimationComplete = false

    override val size: Long =
        run {
            var bytesPerFrame = imageInfo.computeMinByteSize().toLong()
            if (bytesPerFrame <= 0L) {
                // Estimate 4 bytes per pixel.
                bytesPerFrame = 4L * width * height
            }
            // Every frame is kept once it has been decoded, so they all count towards our size.
            (bytesPerFrame * frameCount.coerceAtLeast(1)).coerceAtLeast(0)
        }

    override fun draw(canvas: Canvas) {
        if (frameCount == 0) {
            // The image is empty, nothing to draw.
            return
        }

        if (frameCount == 1) {
            // This is a static image, simply draw it.
            canvas.drawFrame(0)
            return
        }

        if (isAnimationComplete) {
            // The animation is complete, freeze on the last frame.
            canvas.drawFrame(lastDrawnFrameIndex)
            return
        }

        val startTime =
            currentRepetitionStartTime
                ?: TimeSource.Monotonic.markNow().also { currentRepetitionStartTime = it }
        val elapsedTime = startTime.elapsedNow().inWholeMilliseconds

        var accumulatedDuration = 0
        var frameIndexToDraw = frameCount - 1

        // Find the right frame to draw based on the elapsed time.
        for (index in frameDurations.indices) {
            if (accumulatedDuration > elapsedTime) {
                frameIndexToDraw = (index - 1).coerceAtLeast(0)
                break
            }

            accumulatedDuration += frameDurations[index]
        }

        // Remember the last frame we drew; the next time we draw, we'll start from here.
        lastDrawnFrameIndex = frameIndexToDraw

        // Check if we've reached the last frame of the last repetition. If so, we're done.
        // A repetition count of -1 means the animation loops forever; 0 means it is played
        // through exactly once, as the count excludes the first play through.
        isAnimationComplete = repetitionCount >= 0 &&
            currentRepetitionCount >= repetitionCount &&
            frameIndexToDraw == (frameCount - 1)

        canvas.drawFrame(frameIndexToDraw)

        // We still need to wait for the last frame's duration before we start with the next
        // repetition. We only ever land on the last frame when the loop above ran to completion,
        // in which case accumulatedDuration already covers every frame, including that one.
        val drewLastFrame = frameIndexToDraw == frameCount - 1
        val hasLastFrameDurationElapsed = elapsedTime >= accumulatedDuration

        if (!isAnimationComplete && drewLastFrame && hasLastFrameDurationElapsed) {
            // We've reached the last frame of the current repetition, but we can still loop.
            // Reset the state and start over from the first frame.
            lastDrawnFrameIndex = 0
            currentRepetitionCount++
            currentRepetitionStartTime = null
        }

        if (!isAnimationComplete) {
            // Increment this value to force the image to be redrawn.
            invalidateTick++
        }
    }

    /**
     * Decodes a single frame, or returns null if it can't be decoded. A frame we failed to decode
     * is simply not drawn, so one bad frame costs us that frame rather than the whole image.
     */
    private fun decodeFrame(frameIndex: Int): SkiaImage? {
        val frameDecoder = frameDecoder ?: return null
        return try {
            frameDecoder.decodeFrame(
                frameIndex = frameIndex,
                canBlendOverPreviousFrame = canBlendOverPreviousFrame[frameIndex],
            )
        } catch (e: RuntimeException) {
            // Codec throws these at us for frames it can't make sense of.
            null
        }
    }

    private fun releaseFrameDecoder() {
        frameDecoder?.close()
        frameDecoder = null
    }

    private fun Canvas.drawFrame(frameIndex: Int) {
        val frame = frames[frameIndex] ?: decodeFrame(frameIndex)?.also { frames[frameIndex] = it }
        drawImage(
            image = frame ?: return,
            left = 0f,
            top = 0f,
        )
    }
}

/**
 * Decodes frames out of a [Codec] into rasterised images, reusing a single [Bitmap] as scratch
 * space. Holds native memory for as long as it is open.
 */
private class FrameDecoder(
    private val codec: Codec,
    private val imageInfo: ImageInfo,
) : AutoCloseable {
    private val bitmap = Bitmap().apply { allocPixels(codec.imageInfo) }

    /** Index of the frame currently held in [bitmap], or [NO_PRIOR_FRAME] if it holds none. */
    private var bitmapFrameIndex: Int = NO_PRIOR_FRAME

    fun decodeFrame(
        frameIndex: Int,
        canBlendOverPreviousFrame: Boolean,
    ): SkiaImage? {
        // Skia has to decode the whole chain of frames this one depends on, unless we can tell it
        // that the destination bitmap already holds the frame right before this one.
        val priorFrame =
            if (canBlendOverPreviousFrame && bitmapFrameIndex == frameIndex - 1) {
                bitmapFrameIndex
            } else {
                NO_PRIOR_FRAME
            }

        // Until readPixels returns, we can't say what the bitmap holds.
        bitmapFrameIndex = NO_PRIOR_FRAME

        try {
            codec.readPixels(bitmap, frameIndex, priorFrame)
        } catch (e: IllegalArgumentException) {
            if (priorFrame == NO_PRIOR_FRAME) throw e

            // Skia refused the frame we offered as a starting point; decode from scratch instead.
            codec.readPixels(bitmap, frameIndex, NO_PRIOR_FRAME)
        }

        bitmapFrameIndex = frameIndex

        val pixels = bitmap.readPixels(imageInfo, imageInfo.minRowBytes) ?: return null
        return SkiaImage.makeRaster(
            imageInfo = imageInfo,
            bytes = pixels,
            rowBytes = imageInfo.minRowBytes,
        )
    }

    override fun close() {
        bitmap.close()
        codec.close()
    }
}

private val AnimationFrameInfo.safeFrameDuration: Int
    get() = duration.let { if (it <= 0) DEFAULT_FRAME_DURATION else it }

private const val DEFAULT_FRAME_DURATION = 100

/** Tells [Codec.readPixels] that the destination bitmap holds no frame it can build upon. */
private const val NO_PRIOR_FRAME = -1

// Copied from coil3.gif

// https://www.matthewflickinger.com/lab/whatsinagif/bits_and_bytes.asp
private val GIF_HEADER_87A = "GIF87a".encodeUtf8()
private val GIF_HEADER_89A = "GIF89a".encodeUtf8()

// https://developers.google.com/speed/webp/docs/riff_container
private val WEBP_HEADER_RIFF = "RIFF".encodeUtf8()
private val WEBP_HEADER_WEBP = "WEBP".encodeUtf8()
private val WEBP_HEADER_VPX8 = "VP8X".encodeUtf8()

/**
 * Return 'true' if the [source] contains a GIF image. The [source] is not consumed.
 */
private fun isGif(source: BufferedSource): Boolean = source.rangeEquals(0, GIF_HEADER_89A) ||
    source.rangeEquals(0, GIF_HEADER_87A)

/**
 * Return 'true' if the [source] contains a WebP image. The [source] is not consumed.
 */
private fun isWebP(source: BufferedSource): Boolean = source.rangeEquals(0, WEBP_HEADER_RIFF) &&
    source.rangeEquals(8, WEBP_HEADER_WEBP)

/**
 * Return 'true' if the [source] contains an animated WebP image. The [source] is not consumed.
 */
private fun isAnimatedWebP(source: BufferedSource): Boolean = isWebP(source) &&
    source.rangeEquals(12, WEBP_HEADER_VPX8) &&
    source.request(21) &&
    (source.buffer[20] and 0b00000010) > 0
