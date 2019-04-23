package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.File;

final class InstallPackagesUtils {

    static void notifyFinishNotification(
            Context context, NotificationManager notificationManager,
            Notification.Builder builder, boolean install,
            String beOperatedPackageName,
            String title, String text, boolean success) {

        if (beOperatedPackageName == null)
            return;// null 无法取得通知特征 hashcode

        // 小图标
        builder.setSmallIcon(R.drawable.ic_notification);

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
                                            beOperatedPackageName.hashCode(),
                                            resultIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
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
                                        beOperatedPackageName.hashCode(),
                                        new Intent(context, ShowSimpleDialogActivity.class)
                                                .putExtra("title", title)
                                                .putExtra("text", text),
                                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);
            }

            notificationManager.notify(
                    beOperatedPackageName.hashCode(), builder.getNotification()
            );

        } else {

            if (text != null)
                builder.setContentText(text);

            if (title != null)
                builder.setContentTitle(title);

            PendingIntent resultPendingIntent =
                    PendingIntent
                            .getActivity(
                                    context,
                                    beOperatedPackageName.hashCode(),
                                    new Intent(context, ShowSimpleDialogActivity.class)
                                            .putExtra("title", title)
                                            .putExtra("text", text),
                                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            notificationManager.notify(
                    beOperatedPackageName.hashCode(), builder.getNotification()
            );

        }
    }

    /**
     * @param context     Context
     * @param apkFilePath 文件路径
     * @param noCheck     不检查是否为安装过程生成的，直接删除
     */
    static void deleteTempFile(Context context, String apkFilePath, boolean noCheck) {
        if (noCheck || apkFilePath.startsWith(context.getExternalCacheDir() + File.separator + "ZDF-")) {
            File file = new File(apkFilePath);
            if (file.exists()) {
                file.delete();
            }
        }

    }

}
