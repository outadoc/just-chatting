package fr.outadoc.justchatting.utils.coil

import org.jetbrains.skia.Codec

/**
 * Per-frame decode outcome from driving [codec] through the real [AnimatedSkiaImage] code path,
 * alongside the frame metadata Skia reports.
 */
internal data class FrameDiagnostic(
    val index: Int,
    val durationMs: Int,
    val disposalMethod: String,
    val alphaType: String,
    val decoded: Boolean,
)

/**
 * Decodes every frame of [codec] via [AnimatedSkiaImage], same as production, and reports which
 * frames failed alongside their disposal/alpha metadata.
 *
 * Point this at any GIF/WebP dropped into `src/desktopTest/resources/gif/` to see which frame(s)
 * fail and why.
 *
 * Consumes [codec]: prerendering closes it, so don't reuse it afterwards.
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
