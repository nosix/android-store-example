package jp.funmake.example.store

import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.channels.Channel

class StartIntentSenderLauncher(activity: AppCompatActivity) {

    private val resultChannel = Channel<ActivityResult>()

    private val startIntentSenderForResult =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            resultChannel.trySend(it)
        }

    suspend fun launch(sender: IntentSender, callback: (ActivityResult) -> Unit) {
        startIntentSenderForResult.launch(IntentSenderRequest.Builder(sender).build())
        callback(resultChannel.receive())
    }
}