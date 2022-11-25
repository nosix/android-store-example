package jp.funmake.example.store

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
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
    private val sharedMediaStorage = SharedMediaStorage()

    /**
     * アロケートを要求するサイズ
     * 負値を指定すると強制的にアロケートできない扱いにする。
     */
    private val requestAllocateSize = 1.MB

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        Log.d(TAG, "GetContent $uri")
        if (uri != null) {
            val ifd = contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
            FileInputStream(ifd).use {}
            val ofd = contentResolver.openFileDescriptor(uri, "w")?.fileDescriptor
            FileOutputStream(ofd).use {}
        }
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
            Log.d(TAG, "SharedMediaStorage")
            sharedMediaStorage.create(this@MainActivity)
            sharedMediaStorage.read(this@MainActivity)

            if (IS_SUBSCRIBER) {
                getContent.launch("image/*")
                getContent.launch("audio/*")
                getContent.launch("application/*")
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