package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;

public class FUFService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean freeze = intent.getBooleanExtra("freeze",false);
        String[] packages = intent.getStringArrayExtra("packages");
        if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(FUFService.this)) {
            oneKeyActionMRoot(FUFService.this, freeze, packages);
            stopSelf();
        } else {
            oneKeyActionRoot(FUFService.this, null, freeze, packages, false);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_notification);
            mBuilder.setContentText(getString(R.string.freezeAUF));
            NotificationChannel channel = new NotificationChannel("FreezeAndUnfreeze", getString(R.string.freezeAUF), NotificationManager.IMPORTANCE_NONE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId("FreezeAndUnfreeze");
            startForeground(4, mBuilder.build());
        } else {
            startForeground(4, new Notification());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
