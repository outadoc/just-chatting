package com.github.andreyasadchy.xtra.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object DownloadUtils {

    fun savePng(context: Context, folder: String, fileName: String, bitmap: Bitmap) {
        val outputStream: FileOutputStream
        try {
            val path =
                context.filesDir.toString() + File.separator + folder + File.separator + "$fileName.png"
            File(context.filesDir, folder).mkdir()
            outputStream = FileOutputStream(File(path))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
