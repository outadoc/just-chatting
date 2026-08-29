package fr.outadoc.justchatting.utils.coil

import org.jetbrains.skia.Codec

/**
 * Per-frame decode outcome, gathered by decoding [codec] through the real [AnimatedSkiaImage]
 * production code path (not a reimplementation), alongside the frame metadata Skia reports.
 */
internal data class FrameDiagnostic(
    val index: Int,
    val durationMs: Int,
    val disposalMethod: String,
    val alphaType: String,
    val decoded: Boolean,
)

/**
 * Drives [codec] through [AnimatedSkiaImage] exactly as production does (prerendering every
 * frame), then reports which frames failed to decode alongside their disposal/alpha metadata.
 *
 * Point this at any GIF/WebP dropped into `src/desktopTest/resources/gif/` to see exactly which
 * frame(s) drop and why — e.g. an `alphaType` that differs from frame 0's is what causes the
 * `opcrotteRage.gif` regression covered by [AnimatedSkiaImageDecoderTest].
 *
 * Consumes [codec]: prerendering releases the underlying native codec once done, matching what
 * [AnimatedSkiaImageDecoder] does in production, so don't reuse it afterwards.
 */
internal fun diagnoseFrames(codec: Codec): List<FrameDiagnostic> {
    val framesInfo = codec.framesInfo
    val frameCount = codec.frameCount
    val image = AnimatedSkiaImage(codec, prerenderFrames = true)
    return List(frameCount) { index ->
        val info = framesInfo.getOrNull(index)
        FrameDiagnostic(
            index = index,
            durationMs = info?.duration ?: -1,
            disposalMethod = info?.disposalMethod?.toString() ?: "unknown",
            alphaType = info?.alphaType?.toString() ?: "unknown",
            decoded = image.frames.getOrNull(index) != null,
        )
    }
}

internal fun List<FrameDiagnostic>.toReportString(): String =
    joinToString(separator = "\n", prefix = "\n") { d ->
        "frame ${d.index}: duration=${d.durationMs}ms disposal=${d.disposalMethod} " +
            "alphaType=${d.alphaType} decoded=${d.decoded}"
    }
