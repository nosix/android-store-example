package jp.funmake.example.store

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.channels.Channel

class GetContentLauncher(activity: AppCompatActivity) {

    private val uriChannel = Channel<Uri?>()

    private val getContent =
        activity.registerForActivityResult(GetContent()) {
            uriChannel.trySend(it)
        }

    suspend fun launch(mimeType: String, callback: (Uri?) -> Unit) {
        getContent.launch(mimeType)
        callback(uriChannel.receive())
    }
}