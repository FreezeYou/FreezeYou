package cf.playhi.freezeyou.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.allowEditWhenCreateShortcut
import cf.playhi.freezeyou.ui.LauncherShortcutConfirmAndGenerateActivity
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable
import cf.playhi.freezeyou.utils.ToastUtils.showToast

object LauncherShortcutUtils {

    /**
     * @param title Shortcut label
     * @param pkgName Package name
     * @param icon Shortcut icon, drawable
     * @param cls Dest class
     * @param id Unique string
     * @param context Context
     */
    @JvmStatic
    fun checkSettingsAndRequestCreateShortcut(
        title: String,
        pkgName: String,
        icon: Drawable,
        cls: Class<*>,
        id: String,
        context: Context
    ) {
        if (allowEditWhenCreateShortcut.getValue(context)) {
            context.startActivity(
                Intent(
                    context, LauncherShortcutConfirmAndGenerateActivity::class.java
                )
                    .putExtra("pkgName", pkgName)
                    .putExtra("name", title)
                    .putExtra("id", id)
                    .putExtra("class", cls)
            )
        } else {
            createShortCut(title, pkgName, icon, cls, id, context)
        }
    }

    /**
     * @param title Shortcut label
     * @param pkgName Package name
     * @param icon Shortcut icon, drawable
     * @param cls Dest class
     * @param id Unique string
     * @param context Context
     * @param target Target activity. If null, launch the application directly.
     * @param tasks Attached task(s)
     */
    @JvmStatic
    @JvmOverloads
    fun createShortCut(
        title: String,
        pkgName: String,
        icon: Drawable,
        cls: Class<*>,
        id: String,
        context: Context,
        target: String? = null,
        tasks: String? = null
    ) {
        requestCreateShortCut(
            title,
            Intent(context, cls)
                .setAction(Intent.ACTION_MAIN)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("pkgName", pkgName)
                .putExtra("target", target)
                .putExtra("tasks", tasks),
            icon, id, context, null
        )
    }

    /**
     * One of `icon` and `bm` cannot be null,
     * if both `icon` and `bm` is not null,
     * `bm` will be used as the shortcut icon.
     *
     * @param title Shortcut label
     * @param intent Intent which contains `pkgName`, `target` and `tasks`.
     * @param icon Shortcut icon, drawable
     * @param id Unique string
     * @param context Context
     * @param bm Shortcut icon, bitmap
     */
    @JvmStatic
    fun requestCreateShortCut(
        title: String,
        intent: Intent,
        icon: Drawable?,
        id: String,
        context: Context,
        bm: Bitmap?
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestCreateShortCutOldApi(title, intent, icon, context, bm)
        } else {
            val mShortcutManager: ShortcutManager? = context.getSystemService(
                ShortcutManager::class.java
            )
            if (mShortcutManager != null) {
                if (mShortcutManager.isRequestPinShortcutSupported) {
                    val shortcutInfoBuilder = ShortcutInfo.Builder(context, id)
                        .setIcon(
                            Icon.createWithBitmap(
                                bm ?: getBitmapFromDrawable(icon)
                            )
                        )
                        .setIntent(intent)
                        .setShortLabel(title.ifEmpty { " " })
                        .setLongLabel(title.ifEmpty { " " })

                    val pinShortcutInfo = shortcutInfoBuilder.build()
                    // Create the PendingIntent object only if your app needs to be notified
                    // that the user allowed the shortcut to be pinned. Note that, if the
                    // pinning operation fails, your app isn't notified. We assume here that the
                    // app has implemented a method called createShortcutResultIntent() that
                    // returns a broadcast intent.
                    val pinnedShortcutCallbackIntent =
                        mShortcutManager.createShortcutResultIntent(pinShortcutInfo)

                    // Configure the intent so that your app's broadcast receiver gets
                    // the callback successfully.
                    val successCallback = PendingIntent.getBroadcast(
                        context, id.hashCode(),
                        pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE
                    )
                    mShortcutManager.requestPinShortcut(
                        pinShortcutInfo,
                        successCallback.intentSender
                    )
                    showToast(context, R.string.requested)
                } else {
                    requestCreateShortCutOldApi(title, intent, icon, context, bm)
                }
            } else {
                requestCreateShortCutOldApi(title, intent, icon, context, bm)
            }
        }
    }

    /**
     * bm 和 icon 至少有一个不为 null,如均为非空则使用 bm
     *
     * @param title   Title 标题
     * @param intent  Intent
     * @param icon    Icon Drawable 图标
     * @param context Context
     * @param bm      Bitmap 图标
     */
    private fun requestCreateShortCutOldApi(
        title: String,
        intent: Intent,
        icon: Drawable?,
        context: Context,
        bm: Bitmap?
    ) {
        val addShortCut = Intent("com.android.launcher.action.INSTALL_SHORTCUT")

        @Suppress("DEPRECATION")
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)

        @Suppress("DEPRECATION")
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)

        try {
            val bitmap: Bitmap = bm ?: getBitmapFromDrawable(icon)
            var size = context.resources.getDimension(android.R.dimen.app_icon_size)
            if (size < 1) {
                size = 72f
            }
            if (bitmap.height > size) {
                val matrix = Matrix()
                val scaleWidth = size / bitmap.width
                val scaleHeight = size / bitmap.height
                matrix.postScale(scaleWidth, scaleHeight)
                addShortCut.putExtra(
                    @Suppress("DEPRECATION")
                    Intent.EXTRA_SHORTCUT_ICON,
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                )
            } else {
                @Suppress("DEPRECATION")
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
            }
            context.sendBroadcast(addShortCut)
            showToast(context, R.string.requested)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, context.getString(R.string.requestFailed) + e.message)
        }
    }
}