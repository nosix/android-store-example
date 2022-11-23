package jp.funmake.example.store

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AppSpecificInternalStorage {

    fun create(context: Context) {
        // app_Picturesディレクトリを作成する
        val pictureDir = context.getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
        Log.d(TAG, "create $pictureDir")
        // files/Musicディレクトリを作成する
        val musicDir = File(context.filesDir, Environment.DIRECTORY_MUSIC)
        musicDir.mkdirs()
        Log.d(TAG, "create $musicDir")
        // files/fileファイルを作成する
        context.openFileOutput("file", Context.MODE_PRIVATE).use {}
        // files/Music/fileファイルを作成する
        FileOutputStream(File(musicDir, "file")).use {}

        // cache/Musicディレクトリを作成する
        val musicCacheDir = File(context.cacheDir, Environment.DIRECTORY_MUSIC)
        musicCacheDir.mkdirs()
        Log.d(TAG, "create $musicCacheDir")
        // cache/Music/file*.tmpファイルを作成する
        File.createTempFile("file", ".tmp", musicCacheDir)
    }

    fun read(context: Context) {
        // filesの一覧を文字列で取得する
        context.fileList().forEach {
            Log.d(TAG, "fileList $it")
        }
        // filesの一覧をFileで取得する
        context.filesDir.listFiles()?.forEach {
            Log.d(TAG, "fileList $it")
        }
        // filesをルートとするTreeを取得する
        context.filesDir.walk().forEach {
            Log.d(TAG, "walk $it")
        }
        // files/fileを読み込む
        context.openFileInput("file").use {}
        // files/Music/fileを読み込む
        val musicDir = File(context.filesDir, Environment.DIRECTORY_MUSIC)
        FileInputStream(File(musicDir, "file")).use {}

        // cache/Musicの一覧をFileで取得する
        val musicCacheDir = File(context.cacheDir, Environment.DIRECTORY_MUSIC)
        musicCacheDir.listFiles()?.forEach {
            Log.d(TAG, "cacheFileList $it")
        }
    }

    fun delete(context: Context) {
        // app_Picturesディレクトリを削除する
        val pictureDir = context.getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
        pictureDir.deleteRecursively()
        // files/Musicディレクトリを削除する
        val musicDir = File(context.filesDir, Environment.DIRECTORY_MUSIC)
        musicDir.deleteRecursively()
        // files/fileファイルを削除する
        context.deleteFile("file")
        // cache/Musicディレクトリを削除する
        val musicCacheDir = File(context.cacheDir, Environment.DIRECTORY_MUSIC)
        musicCacheDir.deleteRecursively()
    }
}