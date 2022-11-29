package jp.funmake.example.store.launcher

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RequestPermissionsLauncher(activity: AppCompatActivity) :
    ActivityLauncher<Array<String>, Map<String, Boolean>, Boolean>(
        activity,
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
    suspend operator fun invoke(context: Context, permissions: Array<String>): Boolean {
        val require = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toMutableSet()
        if (require.isEmpty()) return true
        return launch(require.toTypedArray()) { result ->
            require.all { result[it] == true }
        }
    }
}