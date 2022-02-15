package cf.playhi.freezeyou.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import net.grandcentrix.tray.AppPreferences;

import cf.playhi.freezeyou.Main;
import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.app.FreezeYouBaseService;
import cf.playhi.freezeyou.listener.ScreenLockListener;

public class ScreenLockOneKeyFreezeService extends FreezeYouBaseService {

    private ScreenLockListener screenLockListener;

    @Override
    public void onCreate() {
        super.onCreate();
        if (new AppPreferences(getApplicationContext()).getBoolean("useForegroundService", false) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(
                            new NotificationChannel(
                                    "BackgroundService", getString(R.string.backgroundService),
                                    NotificationManager.IMPORTANCE_NONE)
                    );
                }
                Notification.Builder mBuilder =
                        new Notification.Builder(this, "BackgroundService");
                mBuilder.setSmallIcon(R.drawable.ic_notification);
                mBuilder.setContentText(getString(R.string.backgroundService));
                Intent resultIntent = new Intent(getApplicationContext(), Main.class);
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                getApplicationContext(), 1, resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                mBuilder.setContentIntent(resultPendingIntent);
                startForeground(1, mBuilder.build());
            } else {
                startForeground(1, new Notification());
            }
        }
        if (screenLockListener == null) {
            screenLockListener = new ScreenLockListener(getApplicationContext());
            screenLockListener.registerListener();
        }
    }

    @Override
    public void onDestroy() {
        if (screenLockListener != null) {
            screenLockListener.unregisterListener();
            screenLockListener = null;
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
