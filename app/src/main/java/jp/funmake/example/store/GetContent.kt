package jp.funmake.example.store

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/**
 * ActivityResultContracts.GetContent を使用するとファイル選択が面倒なので
 * 呼び出し方を合わせつつ、別のアクションにする。
 */
class GetContent : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String): Intent {
        // Intent.ACTION_GET_CONTENT に変更すると
        // ActivityResultContracts.GetContent と同じ
        return Intent(ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(input)
    }

    override fun getSynchronousResult(
        context: Context,
        input: String
    ): SynchronousResult<Uri?>? = null

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
    }
}