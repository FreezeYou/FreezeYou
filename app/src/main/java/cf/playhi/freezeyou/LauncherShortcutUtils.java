package cf.playhi.freezeyou;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;

import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ToastUtils.showToast;

final class LauncherShortcutUtils {

    static void checkSettingsAndRequestCreateShortcut(String title, String pkgName, Drawable icon, Class<?> cls, String id, Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("allowEditWhenCreateShortcut", true)) {
            context.startActivity(
                    new Intent(
                            context, LauncherShortcutConfirmAndGenerateActivity.class)
                            .putExtra("pkgName", pkgName)
                            .putExtra("name", title)
                            .putExtra("id", id)
                            .putExtra("class", cls));
        } else {
            createShortCut(title, pkgName, icon, cls, id, context);
        }
    }

    static void createShortCut(String title, String pkgName, Drawable icon, Class<?> cls, String id, Context context) {
        createShortCut(title, pkgName, icon, cls, id, context, null, null);
    }

//    static void createShortCut(String title, String pkgName, Drawable icon, Class<?> cls, String id, Context context, String target) {
//        createShortCut(title, pkgName, icon, cls, id, context, target, null);
//    }

    static void createShortCut(String title, String pkgName, Drawable icon, Class<?> cls, String id, Context context, String target, String tasks) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            createShortCutOldApi(title, pkgName, icon, cls, context, target, tasks);
//        } else {
        requestCreateShortCut(
                title, new Intent(context, cls)
                        .setAction(Intent.ACTION_MAIN)
                        .putExtra("pkgName", pkgName)
                        .putExtra("target", target)
                        .putExtra("tasks", tasks),
                icon, id, context, null);
//            ShortcutManager mShortcutManager =
//                    context.getSystemService(ShortcutManager.class);
//            if (mShortcutManager != null) {
//                if (mShortcutManager.isRequestPinShortcutSupported()) {
//                    ShortcutInfo.Builder shortcutInfoBuilder =
//                            new ShortcutInfo.Builder(context, id);
//                    shortcutInfoBuilder.setIcon(Icon.createWithBitmap(getBitmapFromDrawable(icon)));
//                    shortcutInfoBuilder.setIntent(
//                            new Intent(context, cls)
//                                    .setAction(Intent.ACTION_MAIN)
//                                    .putExtra("pkgName", pkgName)
//                                    .putExtra("target", target)
//                                    .putExtra("tasks", tasks)
//                    );
//                    shortcutInfoBuilder.setShortLabel(title);
//                    shortcutInfoBuilder.setLongLabel(title);
//
//                    ShortcutInfo pinShortcutInfo = shortcutInfoBuilder.build();
//                    // Create the PendingIntent object only if your app needs to be notified
//                    // that the user allowed the shortcut to be pinned. Note that, if the
//                    // pinning operation fails, your app isn't notified. We assume here that the
//                    // app has implemented a method called createShortcutResultIntent() that
//                    // returns a broadcast intent.
//                    Intent pinnedShortcutCallbackIntent =
//                            mShortcutManager.createShortcutResultIntent(pinShortcutInfo);
//
//                    // Configure the intent so that your app's broadcast receiver gets
//                    // the callback successfully.
//                    PendingIntent successCallback = PendingIntent.getBroadcast(context, id.hashCode(),
//                            pinnedShortcutCallbackIntent, 0);
//
//                    mShortcutManager.requestPinShortcut(pinShortcutInfo,
//                            successCallback.getIntentSender());
//                    showToast(context, R.string.requested);
//                } else {
//                    createShortCutOldApi(title, pkgName, icon, cls, context, target, tasks);
//                }
//            } else {
//                createShortCutOldApi(title, pkgName, icon, cls, context, target, tasks);
//            }
//        }
    }

//    private static void createShortCutOldApi(String title, String pkgName, Drawable icon, Class<?> cls, Context context, String target, String tasks) {
//        Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//        Intent intent = new Intent(context, cls);
//        intent.putExtra("pkgName", pkgName);
//        intent.putExtra("target", target);
//        intent.putExtra("tasks", tasks);
//        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
//        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
//        try {
//            Bitmap bitmap = getBitmapFromDrawable(icon);
//            float size = context.getResources().getDimension(android.R.dimen.app_icon_size);
//            if (size < 1) {
//                size = 72;
//            }
//            if (bitmap.getHeight() > size) {
//                Matrix matrix = new Matrix();
//                float scaleWidth = (size) / bitmap.getWidth();
//                float scaleHeight = (size) / bitmap.getHeight();
//                matrix.postScale(scaleWidth, scaleHeight);
//                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON,
//                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
//                context.sendBroadcast(addShortCut);
//            } else {
//                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
//                context.sendBroadcast(addShortCut);
//            }
//            showToast(context, R.string.requested);
//        } catch (Exception e) {
//            e.printStackTrace();
//            showToast(context, context.getString(R.string.requestFailed) + e.getMessage());
//        }
//    }

    static void requestCreateShortCut(String title, Intent intent, Drawable icon, String id, Context context, Bitmap bm) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestCreateShortCutOldApi(title, intent, icon, context, bm);
        } else {
            ShortcutManager mShortcutManager =
                    context.getSystemService(ShortcutManager.class);
            if (mShortcutManager != null) {
                if (mShortcutManager.isRequestPinShortcutSupported()) {
                    ShortcutInfo.Builder shortcutInfoBuilder =
                            new ShortcutInfo.Builder(context, id);
                    shortcutInfoBuilder.setIcon(
                            Icon.createWithBitmap(bm == null ? getBitmapFromDrawable(icon) : bm));
                    shortcutInfoBuilder.setIntent(intent);
                    shortcutInfoBuilder.setShortLabel(title);
                    shortcutInfoBuilder.setLongLabel(title);

                    ShortcutInfo pinShortcutInfo = shortcutInfoBuilder.build();
                    // Create the PendingIntent object only if your app needs to be notified
                    // that the user allowed the shortcut to be pinned. Note that, if the
                    // pinning operation fails, your app isn't notified. We assume here that the
                    // app has implemented a method called createShortcutResultIntent() that
                    // returns a broadcast intent.
                    Intent pinnedShortcutCallbackIntent =
                            mShortcutManager.createShortcutResultIntent(pinShortcutInfo);

                    // Configure the intent so that your app's broadcast receiver gets
                    // the callback successfully.
                    PendingIntent successCallback = PendingIntent.getBroadcast(context, id.hashCode(),
                            pinnedShortcutCallbackIntent, 0);

                    mShortcutManager.requestPinShortcut(pinShortcutInfo,
                            successCallback.getIntentSender());
                    showToast(context, R.string.requested);
                } else {
                    requestCreateShortCutOldApi(title, intent, icon, context, bm);
                }
            } else {
                requestCreateShortCutOldApi(title, intent, icon, context, bm);
            }
        }
    }

    /**
     * bm 和 icon 至少有一个不为 null,如均为非空则使用 bm
     *
     * @param title   Title
     * @param intent  Intent
     * @param icon    Icon
     * @param context Context
     * @param bm      Bitmap
     */
    private static void requestCreateShortCutOldApi(String title, Intent intent, Drawable icon, Context context, Bitmap bm) {
        Intent addShortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        try {
            Bitmap bitmap;
            if (bm == null) {
                bitmap = getBitmapFromDrawable(icon);
            } else {
                bitmap = bm;
            }
            float size = context.getResources().getDimension(android.R.dimen.app_icon_size);
            if (size < 1) {
                size = 72;
            }
            if (bitmap.getHeight() > size) {
                Matrix matrix = new Matrix();
                float scaleWidth = (size) / bitmap.getWidth();
                float scaleHeight = (size) / bitmap.getHeight();
                matrix.postScale(scaleWidth, scaleHeight);
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
                context.sendBroadcast(addShortCut);
            } else {
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
                context.sendBroadcast(addShortCut);
            }
            showToast(context, R.string.requested);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, context.getString(R.string.requestFailed) + e.getMessage());
        }
    }
}
