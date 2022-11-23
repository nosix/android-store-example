package jp.funmake.example.store

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val appSpecificInternalStorage = AppSpecificInternalStorage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scope.launch {
            appSpecificInternalStorage.create(this@MainActivity)
            appSpecificInternalStorage.read(this@MainActivity)
            appSpecificInternalStorage.delete(this@MainActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun readAppSpecificExternalStorage() {

    }
}