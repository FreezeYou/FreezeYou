package cf.playhi.freezeyou.receiver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.utils.InstallPackagesUtils;

import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.tryDelApkAfterInstalled;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class InstallPackagesFinishedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null)
            return;

        int installStatus = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
        String pkgName = intent.getStringExtra("pkgName");
        String name = intent.getStringExtra("name");

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ?
                new Notification.Builder(context, "InstallPackages") :
                new Notification.Builder(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = null;
        if (installStatus != PackageInstaller.STATUS_SUCCESS) {
            message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (message == null || "".equals(message)) {
                message = context.getString(R.string.unknown);
            }
            message = String.format(context.getString(R.string.reason_colon), message);
        }

        if (intent.getBooleanExtra("install", true)) {
            String apkFilePath = intent.getStringExtra("apkFilePath");

            // Delete Temp File
            InstallPackagesUtils.deleteTempFile(context, apkFilePath, false);

            if (installStatus == PackageInstaller.STATUS_SUCCESS) {
                InstallPackagesUtils
                        .notifyFinishNotification(
                                context, notificationManager, builder,
                                true,
                                pkgName,
                                String.format(context.getString(R.string.app_installFinished), name),
                                null,
                                true);

                if (tryDelApkAfterInstalled.getValue(null))
                    InstallPackagesUtils.deleteTempFile(context, apkFilePath, true);

            } else {
                InstallPackagesUtils
                        .notifyFinishNotification(
                                context, notificationManager, builder,
                                true,
                                pkgName,
                                String.format(context.getString(R.string.app_installFailed), name),
                                message,
                                false);
            }
        } else {
            if (installStatus == PackageInstaller.STATUS_SUCCESS) {
                InstallPackagesUtils
                        .notifyFinishNotification(
                                context, notificationManager, builder,
                                true,
                                pkgName,
                                String.format(context.getString(R.string.app_uninstallFinished), name),
                                null,
                                true);
            } else {
                InstallPackagesUtils
                        .notifyFinishNotification(
                                context, notificationManager, builder,
                                true,
                                pkgName,
                                String.format(context.getString(R.string.app_uninstallFailed), name),
                                message,
                                false);
            }
        }

    }
}
