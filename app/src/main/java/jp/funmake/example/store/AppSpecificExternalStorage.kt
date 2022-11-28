package jp.funmake.example.store

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AppSpecificExternalStorage {

    private fun runIfMounted(action: AppSpecificExternalStorage.() -> Unit) {
        val isMounted = Environment.getExternalStorageState() in setOf(
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY
        )
        if (isMounted) this.action() else {
            Log.d(TAG, "External storages are not mounted.")
        }
    }

    fun create(context: Context) {
        runIfMounted {
            // files/Picturesディレクトリを作成する
            val pictureDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            Log.d(TAG, "AppExternal create $pictureDir")
            // files/fileファイルを作成する
            val filesDir = context.getExternalFilesDir(null)
            FileOutputStream(File(filesDir, "file")).use {}
            // files/Pictures/fileファイルを作成する
            FileOutputStream(File(pictureDir, "file")).use {}

            // cache/Picturesディレクトリを作成する
            val pictureCacheDir = File(context.externalCacheDir, Environment.DIRECTORY_PICTURES)
            pictureCacheDir.mkdirs()
            Log.d(TAG, "AppExternal create $pictureCacheDir")
            // cache/Pictures/file*.tmpファイルを作成する
            File.createTempFile("file", ".tmp", pictureCacheDir)
        }
    }

    fun read(context: Context) {
        runIfMounted {
            // 全てのfilesDirを取得する
            ContextCompat.getExternalFilesDirs(context, null).forEach {
                Log.d(TAG, "AppExternal read externalFilesDirs $it")
            }
            // 全てのcacheDirを取得する
            ContextCompat.getExternalCacheDirs(context).forEach {
                Log.d(TAG, "AppExternal read externalCacheDirs $it")
            }

            // filesの一覧をFileで取得する
            val pictureDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            pictureDir?.listFiles()?.forEach {
                Log.d(TAG, "AppExternal read listFiles $it")
            }
            // files/Pictures/fileを読み込む
            FileInputStream(File(pictureDir, "file")).use {}

            val pictureCacheDir = File(context.externalCacheDir, Environment.DIRECTORY_PICTURES)
            pictureCacheDir.listFiles()?.forEach {
                Log.d(TAG, "AppExternal read cache listFiles $it")
            }
        }
    }

    fun delete(context: Context) {
        runIfMounted {
            // files/Picturesディレクトリを削除する
            val pictureDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            pictureDir?.deleteRecursively()
            // files/fileファイルを削除する
            val filesDir = context.getExternalFilesDir(null)
            File(filesDir, "file").delete()
            // cache/Picturesディレクトリを削除する
            val pictureCacheDir = File(context.externalCacheDir, Environment.DIRECTORY_PICTURES)
            pictureCacheDir.deleteRecursively()
            Log.d(TAG, "AppExternal delete completed")
        }
    }
}