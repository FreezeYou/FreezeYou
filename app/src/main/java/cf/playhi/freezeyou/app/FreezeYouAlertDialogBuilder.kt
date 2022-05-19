package cf.playhi.freezeyou.app

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AlertDialog
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.uiStyleSelection
import cf.playhi.freezeyou.utils.ThemeUtils.getUiTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@JvmOverloads
fun FreezeYouAlertDialogBuilder(
    context: Context,
    overrideThemeResId: Int = 0
): AlertDialog.Builder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        && getUiTheme(context) == uiStyleSelection.defaultValue()
    ) {
        MaterialAlertDialogBuilder(context, overrideThemeResId)
    } else {
        AlertDialog.Builder(context, overrideThemeResId)
    }
}