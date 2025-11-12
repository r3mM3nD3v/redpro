package com.proyecto.red_pro.util

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream

object LocalImages {

    /** Copia un content:// URI a un archivo dentro del almacenamiento interno de la app. */
    fun copyFromContentUri(
        resolver: ContentResolver,
        source: Uri,
        destDir: File,
        destName: String
    ): File? {
        return try {
            if (!destDir.exists()) destDir.mkdirs()
            val dst = File(destDir, destName)
            resolver.openInputStream(source).use { input ->
                FileOutputStream(dst).use { output ->
                    if (input == null) return null
                    input.copyTo(output)
                }
            }
            dst
        } catch (_: Exception) {
            null
        }
    }

    /** Carga un Bitmap desde ruta local a un ImageView (sin librerías). */
    fun loadPathInto(path: String, target: ImageView) {
        try {
            val f = File(path)
            if (f.exists()) {
                val bmp = BitmapFactory.decodeFile(f.absolutePath)
                target.setImageBitmap(bmp)
            }
        } catch (_: Exception) { /* deja placeholder */ }
    }
}