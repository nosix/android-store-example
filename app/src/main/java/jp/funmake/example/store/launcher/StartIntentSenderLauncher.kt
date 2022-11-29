package jp.funmake.example.store.launcher

import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class StartIntentSenderLauncher(activity: AppCompatActivity) :
    ActivityLauncher<IntentSenderRequest, ActivityResult, Unit>(
        activity,
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
    suspend operator fun invoke(sender: IntentSender, callback: (ActivityResult) -> Unit) {
        launch(IntentSenderRequest.Builder(sender).build(), callback)
    }
}