package jp.funmake.example.store

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

class FileSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_select)

        val file = when (intent.type) {
            "image/*" -> pickFileFromAppSpecificExternalStorage()
            "audio/*" -> pickFileFromAppSpecificInternalStorage()
            "application/*" -> pickCacheFileFromAppSpecificInternalStorage()
            else -> null
        }

        if (file != null) {
            Log.d(TAG, "$file")
            val uri =
                FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
            Log.d(TAG, "$uri")

            val resultIntent = Intent("jp.funmake.example.store.ACTION_RETURN_FILE").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                setDataAndType(uri, contentResolver.getType(uri))
            }
            setResult(RESULT_OK, resultIntent)
        } else {
            setResult(RESULT_CANCELED, null)
        }

        finish()
    }

    private fun pickFileFromAppSpecificInternalStorage(): File? {
        return File(filesDir, Environment.DIRECTORY_MUSIC).listFiles()?.firstOrNull()
    }

    private fun pickCacheFileFromAppSpecificInternalStorage(): File? {
        return File(cacheDir, Environment.DIRECTORY_MUSIC).listFiles()?.firstOrNull()
    }

    private fun pickFileFromAppSpecificExternalStorage(): File? {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.listFiles()?.firstOrNull()
    }
}