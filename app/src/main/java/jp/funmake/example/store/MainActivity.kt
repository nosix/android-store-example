package jp.funmake.example.store

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val appSpecificInternalStorage = AppSpecificInternalStorage()
    private val appSpecificExternalStorage = AppSpecificExternalStorage()

    /**
     * アロケートを要求するサイズ
     * 負値を指定すると強制的にアロケートできない扱いにする。
     */
    private val requestAllocateSize = 1.MB

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
            }
        }
        scope.launch {
            Log.d(TAG, "AppSpecificInternalStorage")
            appSpecificInternalStorage.create(this@MainActivity)
            appSpecificInternalStorage.read(this@MainActivity)
            Log.d(TAG, "AppSpecificExternalStorage")
            appSpecificExternalStorage.create(this@MainActivity)
            appSpecificExternalStorage.read(this@MainActivity)
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