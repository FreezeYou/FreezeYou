package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import cf.playhi.freezeyou.app.FreezeYouBaseService;
import cf.playhi.freezeyou.utils.ForceStopUtils;

public class ForceStopService extends FreezeYouBaseService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();

        String[] packages = intent.getStringArrayExtra("packages");
        ForceStopUtils.forceStop(context, packages);

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_notification);
            mBuilder.setContentText(getString(R.string.forceStop));
            NotificationChannel channel = new NotificationChannel("ForceStop", getString(R.string.forceStop), NotificationManager.IMPORTANCE_NONE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId("ForceStop");
            startForeground(6, mBuilder.build());
        } else {
            startForeground(6, new Notification());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
