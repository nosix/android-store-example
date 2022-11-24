package jp.funmake.example.store

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log

class SharedMediaStorage {

    private val imagesUri
        get() = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    private val videoUri
        get() = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

    private val audioUri
        get() = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    private val downloadUri
        get() = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            throw IllegalStateException("not supported")
        }

    private val filesUri
        get() = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            throw IllegalStateException("not supported")
        }

    fun create(context: Context) {
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        context.contentResolver.insert(imagesUri, content)?.let { fileUri ->
            context.contentResolver.openOutputStream(fileUri).use {}
            Log.d(TAG, "create $fileUri")
        }
    }

    fun read(context: Context) {
        context.contentResolver.query(
            imagesUri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            Log.d(TAG, "read ${cursor.columnNames.toList()}")
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            while (cursor.moveToNext()) {
                Log.d(TAG, "${cursor.getString(displayNameIndex)} : ${cursor.getString(dataIndex)}")
            }
        }
    }

    fun delete(context: Context) {
        context.contentResolver.delete(imagesUri, null, null)
    }
}