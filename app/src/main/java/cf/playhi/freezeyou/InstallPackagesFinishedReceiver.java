package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.File;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ToastUtils.showToast;

public class InstallPackagesFinishedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
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
                if (!file.delete())
                    if (!file.delete())
                        showToast(context, "Cannot delete temp file: " + apkFilePath);
            }
            builder.setContentTitle(name + " 安装完成");
            builder.setLargeIcon(getBitmapFromDrawable(getApplicationIcon(context, pkgName, null, false)));
        } else {
            builder.setContentTitle(name + " 卸载完成");
        }

        // Notify notification
        builder.setSmallIcon(R.drawable.ic_notification);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(pkgName.hashCode(), builder.getNotification());
    }
}
