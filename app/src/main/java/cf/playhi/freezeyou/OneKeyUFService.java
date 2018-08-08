package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;

public class OneKeyUFService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_notification);
            mBuilder.setContentText(getString(R.string.oneKeyUF));
            NotificationChannel channel = new NotificationChannel("OneKeyUF", getString(R.string.oneKeyUF), NotificationManager.IMPORTANCE_NONE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId("OneKeyUF");
            startForeground(3, mBuilder.build());
        } else {
            startForeground(3, new Notification());
        }
        String[] pkgNames = getApplicationContext().getSharedPreferences(
                getString(R.string.sOneKeyUFApplicationList), Context.MODE_PRIVATE).getString("pkgName", "").split(",");
        if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(this)) {
            oneKeyActionMRoot(this, false, pkgNames);
            doFinish();
        } else {
            oneKeyActionRoot(this, null, false, pkgNames, false);
            doFinish();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void doFinish() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(3);
        }
        stopSelf();
    }
}
