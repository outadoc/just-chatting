package fr.outadoc.justchatting.feature.chat.presentation

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

internal class CoilReducedAnimationTransformation : Transformation {

    override val cacheKey: String = "CoilReducedAnimationTransformation"
    override suspend fun transform(input: Bitmap, size: Size): Bitmap = input
}
