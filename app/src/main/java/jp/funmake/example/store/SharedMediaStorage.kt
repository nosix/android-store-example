package jp.funmake.example.store

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log

/**
 * @param onlySelfMedia 自アプリで作成したファイルのみ読み込む場合は true
 */
class SharedMediaStorage(onlySelfMedia: Boolean) {

    val readPermissions = if (onlySelfMedia) {
        emptyArray()
    } else {
        when {
            Build.VERSION.SDK_INT >= 33 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    val writePermissions = when {
        Build.VERSION.SDK_INT >= 29 -> emptyArray()
        else -> arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

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
        val suffix = BuildConfig.APPLICATION_ID.split('.').last()
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_$suffix")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        context.contentResolver.insert(imagesUri, content)?.let { fileUri ->
            context.contentResolver.openOutputStream(fileUri).use {}
            Log.d(TAG, "SharedMedia create $fileUri")
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
            Log.d(TAG, "SharedMedia read ${cursor.columnNames.toList()}")
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            while (cursor.moveToNext()) {
                Log.d(
                    TAG,
                    "SharedMedia read ${cursor.getString(displayNameIndex)} ${
                        cursor.getString(dataIndex)
                    }"
                )
            }
        }
    }

    fun delete(context: Context) {
        context.contentResolver.delete(imagesUri, null, null)
        Log.d(TAG, "SharedMedia delete completed")
    }
}