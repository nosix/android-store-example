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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestSpace(10.MB)
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
    private fun requestSpace(bytes: Long) {
        val storageManager = checkNotNull(getSystemService<StorageManager>())
        val uuid = storageManager.getUuidForPath(filesDir)
        val allocatableBytes = storageManager.getAllocatableBytes(uuid)
        Log.d(TAG, "allocatable: ${allocatableBytes.withUnit}")
        if (bytes <= allocatableBytes) {
            storageManager.allocateBytes(uuid, bytes)
        } else {
            startActivity(Intent(StorageManager.ACTION_MANAGE_STORAGE))
            //startActivity(Intent(StorageManager.ACTION_CLEAR_APP_CACHE))
        }
    }
}