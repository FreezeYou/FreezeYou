package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

import static cf.playhi.freezeyou.Support.doLockScreen;
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
        String[] pkgNameList = getApplicationContext().getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName", "").split("\\|\\|");
        if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(getApplicationContext())) {
            oneKeyActionMRoot(this, true, pkgNameList);
            checkAuto(auto, this);
        } else {
            oneKeyActionRoot(this, null, true, pkgNameList, false);
            checkAuto(auto, this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void checkAndLockScreen(Context context) {
        switch (PreferenceManager.getDefaultSharedPreferences(context).getString("shortCutOneKeyFreezeAdditionalOptions", "nothing")) {
            case "nothing":
                doFinish();
                break;
            case "askLockScreen":
                startActivity(new Intent(getApplicationContext(), AskLockScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                doFinish();
                break;
            case "lockScreenImmediately":
                doLockScreen(context);
                doFinish();
                break;
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
