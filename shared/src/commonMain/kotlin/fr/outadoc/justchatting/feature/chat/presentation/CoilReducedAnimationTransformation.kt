package fr.outadoc.justchatting.feature.chat.presentation

import coil3.Bitmap
import coil3.size.Size
import coil3.transform.Transformation

public class CoilReducedAnimationTransformation : Transformation() {
    override val cacheKey: String = "CoilReducedAnimationTransformation"

    override suspend fun transform(
        input: Bitmap,
        size: Size,
    ): Bitmap = input
}
