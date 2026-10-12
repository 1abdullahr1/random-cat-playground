package com.example.catplayground.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExporter {

    suspend fun saveToGallery(context: Context, bitmap: Bitmap, title: String = "CatPlayground"): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "Cat_${System.currentTimeMillis()}.jpg"
                val resolver = context.contentResolver

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/CatPlayground")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext false

                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun saveToCacheForShare(context: Context, bitmap: Bitmap): File? =
        withContext(Dispatchers.IO) {
            try {
                val shareDir = File(context.cacheDir, "cats").apply {
                    if (!exists()) mkdirs()
                }
                val shareFile = File(shareDir, "cat_share_${System.currentTimeMillis()}.jpg")
                FileOutputStream(shareFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                }
                shareFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
