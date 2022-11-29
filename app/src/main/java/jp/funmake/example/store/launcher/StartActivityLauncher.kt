package jp.funmake.example.store.launcher

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class StartActivityLauncher(activity: AppCompatActivity) :
    ActivityLauncher<Intent, ActivityResult, Unit>(
        activity,
        ActivityResultContracts.StartActivityForResult()
    ) {
    suspend operator fun invoke(intent: Intent, callback: (ActivityResult) -> Unit) {
        launch(intent, callback)
    }
}