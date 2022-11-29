package jp.funmake.example.store

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import jp.funmake.example.store.launcher.StartIntentSenderLauncher

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

    suspend fun readAndUpdate(context: Context, startIntentSender: StartIntentSenderLauncher) {
        val resolver = context.contentResolver
        resolver.query(
            imagesUri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            Log.d(TAG, "SharedMedia read ${cursor.columnNames.toList()}")
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val packageIndex = if (Build.VERSION.SDK_INT >= 29) {
                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.OWNER_PACKAGE_NAME)
            } else {
                0
            }
            val writeContext = WriteContext(context, startIntentSender)
            while (cursor.moveToNext()) {
                val displayName = cursor.getString(displayNameIndex)
                val data = cursor.getString(dataIndex)
                Log.d(
                    TAG,
                    "SharedMedia read $displayName $data"
                )
                val id = cursor.getLong(idIndex)
                val uri = ContentUris.withAppendedId(imagesUri, id)
                val hasPermission = packageIndex == 0
                        || cursor.getString(packageIndex) == BuildConfig.APPLICATION_ID
                writeContext.write(uri, hasPermission) {
                    resolver.openOutputStream(uri)?.use { out ->
                        out.write(3)
                    }
                }
                resolver.openInputStream(uri)?.use {
                    Log.d(TAG, "SharedMedia read stream ${it.read()}")
                }
            }
        }
    }

    fun delete(context: Context) {
        context.contentResolver.delete(imagesUri, null, null)
        Log.d(TAG, "SharedMedia delete completed")
    }

    private class WriteContext(
        private val context: Context,
        private val startIntentSender: StartIntentSenderLauncher
    ) {
        suspend fun write(uri: Uri, hasPermission: Boolean, writeAction: (Uri) -> Unit) {
            if (hasPermission) {
                writeAction(uri)
                return
            }
            val resolver = context.contentResolver
            when {
                Build.VERSION.SDK_INT >= 30 -> {
                    val pendingIntent = MediaStore.createWriteRequest(
                        resolver,
                        listOf(uri)
                    )
                    startIntentSender(pendingIntent.intentSender) { result ->
                        if (result.resultCode == Activity.RESULT_OK) writeAction(uri)
                    }
                }
                Build.VERSION.SDK_INT >= 29 -> {
                    try {
                        writeAction(uri)
                    } catch (e: RecoverableSecurityException) {
                        startIntentSender(e.userAction.actionIntent.intentSender) { result ->
                            if (result.resultCode == Activity.RESULT_OK) writeAction(uri)
                        }
                    }
                }
                else -> writeAction(uri)
            }
        }
    }
}