package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.DevicePolicyManagerUtils.doLockScreen;
import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;

public class OneKeyFreezeService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_notification);
            mBuilder.setContentText(getString(R.string.oneKeyFreeze));
            NotificationChannel channel = new NotificationChannel("OneKeyFreeze", getString(R.string.oneKeyFreeze), NotificationManager.IMPORTANCE_NONE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId("OneKeyFreeze");
            startForeground(2, mBuilder.build());
        } else {
            startForeground(2, new Notification());
        }
        boolean auto = intent.getBooleanExtra("autoCheckAndLockScreen", true);
        String pkgNames = new AppPreferences(getApplicationContext()).getString(getString(R.string.sAutoFreezeApplicationList), "");
        if (pkgNames != null) {
            if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(getApplicationContext())) {
                oneKeyActionMRoot(this, true, pkgNames.split(","));
                checkAuto(auto, this);
            } else {
                oneKeyActionRoot(this, true, pkgNames.split(","));
                checkAuto(auto, this);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkAndLockScreen(Context context) {
        String options = new AppPreferences(getApplicationContext()).getString("shortCutOneKeyFreezeAdditionalOptions", "nothing");
        if (options == null)
            options = "";
        switch (options) {
            case "askLockScreen":
                startActivity(new Intent(getApplicationContext(), AskLockScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                doFinish();
                break;
            case "lockScreenImmediately":
                doLockScreen(context);
                doFinish();
                break;
            case "nothing":
            default:
                doFinish();
                break;
        }
    }

    private void doFinish() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(2);
        }
        stopSelf();
    }

    private void checkAuto(boolean auto, Context context) {
        if (auto) {
            checkAndLockScreen(context);
        } else {
            doFinish();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
