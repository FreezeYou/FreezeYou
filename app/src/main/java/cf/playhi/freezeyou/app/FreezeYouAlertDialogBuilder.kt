package cf.playhi.freezeyou.app

import android.content.Context
import androidx.appcompat.app.AlertDialog
import cf.playhi.freezeyou.utils.ThemeUtils.isMaterial3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@JvmOverloads
fun FreezeYouAlertDialogBuilder(
    context: Context,
    overrideThemeResId: Int = 0
): AlertDialog.Builder {
    return if (isMaterial3Theme()) {
        MaterialAlertDialogBuilder(context, overrideThemeResId)
    } else {
        AlertDialog.Builder(context, overrideThemeResId)
    }
}