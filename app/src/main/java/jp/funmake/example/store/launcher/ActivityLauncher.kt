package jp.funmake.example.store.launcher

import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.channels.Channel

abstract class ActivityLauncher<I, O, R>(
    activity: AppCompatActivity,
    contract: ActivityResultContract<I, O>
) {
    private val resultChannel = Channel<O>()

    private val startActivity = activity.registerForActivityResult(contract) {
        resultChannel.trySend(it)
    }

    protected suspend fun launch(input: I, callback: (O) -> R): R {
        startActivity.launch(input)
        return callback(resultChannel.receive())
    }
}