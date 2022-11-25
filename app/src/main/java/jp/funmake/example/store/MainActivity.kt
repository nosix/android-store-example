package jp.funmake.example.store

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val appSpecificInternalStorage = AppSpecificInternalStorage()
    private val appSpecificExternalStorage = AppSpecificExternalStorage()
    private val sharedMediaStorage = SharedMediaStorage()

    /**
     * アロケートを要求するサイズ
     * 負値を指定すると強制的にアロケートできない扱いにする。
     */
    private val requestAllocateSize = 1.MB

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.d(TAG, "GetContent $uri")
            if (uri != null) {
                contentResolver.query(uri, null, null, null, null)?.let { cursor ->
                    val displayNameIndex =
                        cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    Log.d(TAG, "${cursor.getString(displayNameIndex)} ${cursor.getLong(sizeIndex)}")
                }
                val ifd = contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
                FileInputStream(ifd).use {}
                val ofd = contentResolver.openFileDescriptor(uri, "w")?.fileDescriptor
                FileOutputStream(ofd).use {}
            }
        }

    private val grantMessage = Channel<Map.Entry<String, Boolean>>()

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            scope.launch {
                granted.forEach {
                    grantMessage.send(it)
                }
            }
        }

    private suspend fun hasPermissions(permissions: Array<String>): Boolean {
        requestPermissions.launch(permissions)
        var result = true
        val waiting = permissions.toMutableSet()
        while (waiting.isNotEmpty()) {
            val granted = grantMessage.receive()
            check(waiting.remove(granted.key))
            result = result && granted.value
        }
        return result
    }

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

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            scope.launch {
                appSpecificInternalStorage.delete(this@MainActivity)
                appSpecificExternalStorage.delete(this@MainActivity)
                sharedMediaStorage.delete(this@MainActivity)
            }
        }
        scope.launch {
            Log.d(TAG, "AppSpecificInternalStorage")
            appSpecificInternalStorage.create(this@MainActivity)
            appSpecificInternalStorage.read(this@MainActivity)
            Log.d(TAG, "AppSpecificExternalStorage")
            appSpecificExternalStorage.create(this@MainActivity)
            appSpecificExternalStorage.read(this@MainActivity)

            if (IS_SUBSCRIBER) {
                getContent.launch("image/*")
                getContent.launch("audio/*")
                getContent.launch("application/*")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        scope.launch {
            val hasPermissions = if (Build.VERSION.SDK_INT >= 29) true else {
                hasPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
            if (hasPermissions) {
                Log.d(TAG, "SharedMediaStorage")
                sharedMediaStorage.create(this@MainActivity)
                sharedMediaStorage.read(this@MainActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
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