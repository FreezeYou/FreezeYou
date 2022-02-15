package cf.playhi.freezeyou.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.ui.ShowSimpleDialogActivity;

public final class InstallPackagesUtils {

    public static void notifyFinishNotification(
            Context context, NotificationManager notificationManager,
            Notification.Builder builder, boolean install,
            String beOperatedPackageName,
            String title, String text, boolean success) {

        if (beOperatedPackageName == null)
            return;// null 无法取得通知特征 hashcode

        // 小图标
        builder.setSmallIcon(R.drawable.ic_notification);

        builder.setProgress(0, 0, false);

        if (install) {

            // 提示标题
            if (title != null)
                builder.setContentTitle(title);

            // 提示文本
            if (text != null)
                builder.setContentText(text);

            if (success) {

                // 大图标
                builder.setLargeIcon(
                        ApplicationIconUtils.getBitmapFromDrawable(
                                ApplicationIconUtils.getApplicationIcon(
                                        context,
                                        beOperatedPackageName,
                                        ApplicationInfoUtils
                                                .getApplicationInfoFromPkgName(
                                                        beOperatedPackageName,
                                                        context),
                                        false)
                        )
                );

                // 点击打开
                Intent resultIntent =
                        context.getPackageManager().getLaunchIntentForPackage(beOperatedPackageName);
                if (resultIntent != null) {
                    PendingIntent resultPendingIntent =
                            PendingIntent
                                    .getActivity(
                                            context,
                                            (beOperatedPackageName + "@InstallPackagesNotification").hashCode(),
                                            resultIntent,
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                                    ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                                    : PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(resultPendingIntent);
                    builder.setAutoCancel(true);

                    if (text == null)
                        builder.setContentText(context.getString(R.string.openImmediately));

                }
            } else {
                // 错误信息弹窗
                PendingIntent resultPendingIntent =
                        PendingIntent
                                .getActivity(
                                        context,
                                        (beOperatedPackageName + "@InstallPackagesNotification").hashCode(),
                                        new Intent(context, ShowSimpleDialogActivity.class)
                                                .putExtra("title", title)
                                                .putExtra("text", text),
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                                : PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);
            }

        } else {

            if (text != null)
                builder.setContentText(text);

            if (title != null)
                builder.setContentTitle(title);

            PendingIntent resultPendingIntent =
                    PendingIntent
                            .getActivity(
                                    context,
                                    (beOperatedPackageName + "@InstallPackagesNotification").hashCode(),
                                    new Intent(context, ShowSimpleDialogActivity.class)
                                            .putExtra("title", title)
                                            .putExtra("text", text),
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                            ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                            : PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

        }

        notificationManager.notify(
                (beOperatedPackageName + "@InstallPackagesNotification").hashCode(), builder.build()
        );

    }

    /**
     * @param context     Context
     * @param apkFilePath 文件路径
     * @param noCheck     不检查是否为安装过程生成的，直接删除
     */
    public static void deleteTempFile(Context context, String apkFilePath, boolean noCheck) {
        if (noCheck || apkFilePath.startsWith(context.getExternalCacheDir() + File.separator + "ZDF-")) {
            File file = new File(apkFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static void postWaitingForLeavingToInstallApplicationNotification(Context context, PackageInfo packageInfo) {

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ?
                new Notification.Builder(context, "InstallPackages") :
                new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        PackageManager pm = context.getPackageManager();

        builder.setContentTitle(
                String.format(
                        context.getString(R.string.waitingToInstall_app),
                        pm.getApplicationLabel(packageInfo.applicationInfo)
                )
        );
        builder.setProgress(100, 0, true);
        builder.setLargeIcon(
                ApplicationIconUtils.getBitmapFromDrawable(
                        pm.getApplicationIcon(packageInfo.applicationInfo)
                )
        );
        notificationManager.notify(
                (packageInfo.packageName + "@InstallPackagesNotification").hashCode(),
                builder.build()
        );

    }

}
