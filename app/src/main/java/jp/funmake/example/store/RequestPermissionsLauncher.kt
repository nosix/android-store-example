package jp.funmake.example.store

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.Channel

class RequestPermissionsLauncher(activity: AppCompatActivity) {

    private val resultChannel = Channel<Map<String, Boolean>>()

    private val requestPermissions =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            resultChannel.trySend(it)
        }

    suspend fun launch(context: Context, permissions: Array<String>): Boolean {
        val require = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toMutableSet()
        if (require.isEmpty()) return true
        requestPermissions.launch(require.toTypedArray())
        val result = resultChannel.receive()
        return require.all { result[it] == true }
    }
}