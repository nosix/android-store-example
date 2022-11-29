package jp.funmake.example.store.launcher

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import jp.funmake.example.store.GetContent

class GetContentLauncher(activity: AppCompatActivity) :
    ActivityLauncher<String, Uri?, Unit>(
        activity,
        GetContent()
    ) {
    suspend operator fun invoke(mimeType: String, callback: (Uri?) -> Unit) {
        launch(mimeType, callback)
    }
}