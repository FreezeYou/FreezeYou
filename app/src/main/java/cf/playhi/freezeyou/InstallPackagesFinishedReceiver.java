package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

import java.io.File;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;

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

        if (intent.getBooleanExtra("install", true)) {
            String apkFilePath = intent.getStringExtra("apkFilePath");
            // Delete Temp File
            File file = new File(apkFilePath);
            if (file.exists()) {
                file.delete();
            }

            if (installStatus == PackageInstaller.STATUS_SUCCESS) {
                builder.setContentTitle(name + " " + context.getString(R.string.installFinished));
                builder.setLargeIcon(getBitmapFromDrawable(getApplicationIcon(context, pkgName, null, false)));
            } else {
                builder.setContentTitle(name + " " + context.getString(R.string.installFailed));
            }
        } else {
            if (installStatus == PackageInstaller.STATUS_SUCCESS) {
                builder.setContentTitle(name + " " + context.getString(R.string.uninstallFinished));
            } else {
                builder.setContentTitle(name + " " + context.getString(R.string.uninstallFailed));
            }
        }

        if (installStatus != PackageInstaller.STATUS_SUCCESS) {
            String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (message == null || "".equals(message)) {
                message = context.getString(R.string.unknown);
            }
            message = String.format(context.getString(R.string.reason_colon), message);
            builder.setContentText(message);
        }

        // Notify notification
        builder.setSmallIcon(R.drawable.ic_notification);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(pkgName.hashCode(), builder.getNotification());

    }
}
