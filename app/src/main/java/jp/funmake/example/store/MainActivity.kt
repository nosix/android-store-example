package jp.funmake.example.store

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        scope.launch {
            Log.d(TAG, "AppSpecificInternalStorage")
            appSpecificInternalStorage.create(this@MainActivity)
            appSpecificInternalStorage.read(this@MainActivity)
            appSpecificInternalStorage.delete(this@MainActivity)
            Log.d(TAG, "AppSpecificExternalStorage")
            appSpecificExternalStorage.create(this@MainActivity)
            appSpecificExternalStorage.read(this@MainActivity)
            appSpecificExternalStorage.delete(this@MainActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}