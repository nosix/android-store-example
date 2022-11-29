package jp.funmake.example.store

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val appSpecificInternalStorage = AppSpecificInternalStorage()
    private val appSpecificExternalStorage = AppSpecificExternalStorage()
    private val sharedMediaStorage = SharedMediaStorage(false)
    private val documentStorage = DocumentStorage()

    /**
     * アロケートを要求するサイズ
     * 負値を指定すると強制的にアロケートできない扱いにする。
     */
    private val requestAllocateSize = 1.MB

    private val requestPermissionsLauncher = RequestPermissionsLauncher(this)
    private val startActivityLauncher = StartActivityLauncher(this)
    private val startIntentSenderLauncher = StartIntentSenderLauncher(this)
    private val getContentLauncher = GetContentLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestSpace(requestAllocateSize) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivity(Intent(StorageManager.ACTION_MANAGE_STORAGE))
//                    startActivity(Intent(StorageManager.ACTION_CLEAR_APP_CACHE))
                }
            }
        }

        findViewById<Button>(R.id.createButton).setOnClickListener {
            scope.launch {
                appSpecificInternalStorage.create(this@MainActivity)
                appSpecificExternalStorage.create(this@MainActivity)
                if (hasPermissions(sharedMediaStorage.writePermissions)) {
                    sharedMediaStorage.create(this@MainActivity)
                }
                documentStorage.create(startActivityLauncher)
            }
        }
        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            scope.launch {
                appSpecificInternalStorage.delete(this@MainActivity)
                appSpecificExternalStorage.delete(this@MainActivity)
                sharedMediaStorage.delete(this@MainActivity)
                documentStorage.delete(this@MainActivity, startActivityLauncher)
            }
        }
        findViewById<Button>(R.id.readAndUpdateButton).setOnClickListener {
            scope.launch {
                appSpecificInternalStorage.readAndUpdate(this@MainActivity)
                appSpecificExternalStorage.readAndUpdate(this@MainActivity)
                if (hasPermissions(sharedMediaStorage.readPermissions)) {
                    sharedMediaStorage.readAndUpdate(this@MainActivity, startIntentSenderLauncher)
                }
                documentStorage.readAndUpdate(this@MainActivity, startActivityLauncher)
                if (IS_SUBSCRIBER) {
                    getContent("image/*")
                    getContent("audio/*")
                    getContent("application/*")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private suspend fun hasPermissions(permissions: Array<String>): Boolean {
        return requestPermissionsLauncher.launch(this, permissions)
    }

    private suspend fun getContent(mimeType: String) {
        getContentLauncher.launch(mimeType) { uri ->
            Log.d(TAG, "GetContent $uri")
            if (uri != null) {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val displayNameIndex =
                        cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    Log.d(
                        TAG,
                        "GetContent ${cursor.getString(displayNameIndex)} ${cursor.getLong(sizeIndex)}"
                    )
                }
                contentResolver.openFileDescriptor(uri, "w")?.use { ofd ->
                    FileOutputStream(ofd.fileDescriptor).write(6)
                }
                contentResolver.openFileDescriptor(uri, "r")?.use { ifd ->
                    val data = FileInputStream(ifd.fileDescriptor).read()
                    Log.d(TAG, "GetContent read stream $data")
                }
            }
        }
    }

    @RequiresApi(26)
    private fun requestSpace(bytes: Long, whenNotAllocatable: () -> Unit) {
        val storageManager = checkNotNull(getSystemService<StorageManager>())
        val uuid = storageManager.getUuidForPath(filesDir)
        val allocatableBytes = storageManager.getAllocatableBytes(uuid)
        Log.d(TAG, "allocatable: ${allocatableBytes.withUnit}")
        if (bytes in 0..allocatableBytes) {
            storageManager.allocateBytes(uuid, bytes)
        } else {
            whenNotAllocatable()
        }
    }
}