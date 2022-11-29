package jp.funmake.example.store

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.documentfile.provider.DocumentFile
import jp.funmake.example.store.launcher.StartActivityLauncher

class DocumentStorage {

    suspend fun create(startActivity: StartActivityLauncher) {
        val suffix = BuildConfig.APPLICATION_ID.split('.').last()
        Intent(Intent.ACTION_CREATE_DOCUMENT).run {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "file_$suffix")
            startActivity(this) { result ->
                Log.d(TAG, "Document create ${result.uri}")
            }
        }
    }

    suspend fun readAndUpdate(context: Context, startActivity: StartActivityLauncher) {
        val resolver = context.contentResolver
        Intent(Intent.ACTION_OPEN_DOCUMENT).run {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            startActivity(this) { result ->
                Log.d(TAG, "Document read ${result.uri}")
                result.uri?.let { uri ->
                    resolver.openOutputStream(uri)?.use {
                        it.write(4)
                    }
                    resolver.openInputStream(uri)?.use {
                        Log.d(TAG, "Document read stream ${it.read()}")
                    }
                }
            }
        }
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).run {
            startActivity(this) { result ->
                Log.d(TAG, "Document read ${result.uri}")
                result.uri?.let { uri ->
                    DocumentFile.fromTreeUri(context, uri)?.let { dir ->
                        for (file in dir.listFiles()) {
                            Log.d(TAG, "Document read ${file.name}")
                            resolver.openOutputStream(file.uri)?.use {
                                it.write(5)
                            }
                            resolver.openInputStream(file.uri)?.use {
                                Log.d(TAG, "Document read stream ${it.read()}")
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun delete(context: Context, startActivity: StartActivityLauncher) {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).run {
            startActivity(this) { result ->
                result.uri?.let { uri ->
                    DocumentFile.fromTreeUri(context, uri)?.let { dir ->
                        for (file in dir.listFiles()) {
                            DocumentsContract.deleteDocument(context.contentResolver, file.uri)
                        }
                        Log.d(TAG, "Document delete completed")
                    }
                }
            }
        }
    }

    private val ActivityResult.uri: Uri?
        get() = takeIf { resultCode == Activity.RESULT_OK }?.data?.data
}