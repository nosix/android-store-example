package jp.funmake.example.store

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.channels.Channel

class StartActivityLauncher(activity: AppCompatActivity) {

    private val resultChannel = Channel<ActivityResult>()

    private val startActivityForResult =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            resultChannel.trySend(it)
        }

    suspend fun launch(intent: Intent, callback: (ActivityResult) -> Unit) {
        startActivityForResult.launch(intent)
        callback(resultChannel.receive())
    }
}