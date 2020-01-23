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

import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner;
import static cf.playhi.freezeyou.utils.FUFUtils.checkMRootFrozen;
import static cf.playhi.freezeyou.utils.FUFUtils.oneKeyActionMRoot;
import static cf.playhi.freezeyou.utils.FUFUtils.oneKeyActionRoot;
import static cf.playhi.freezeyou.utils.FUFUtils.processMRootAction;
import static cf.playhi.freezeyou.utils.FUFUtils.processRootAction;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class FUFService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean freeze = intent.getBooleanExtra("freeze", false);
        Context context = getApplicationContext();
        if (intent.getBooleanExtra("single", false)) {
            String pkgName = intent.getStringExtra("pkgName");
            String target = intent.getStringExtra("target");
            String tasks = intent.getStringExtra("tasks");
            boolean askRun = intent.getBooleanExtra("askRun", false);
            boolean runImmediately = intent.getBooleanExtra("runImmediately", false);
            if (freeze) {
                if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
                    if (processMRootAction(context, pkgName, target, tasks, true, askRun, false, null, false)) {
                        if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                            showToast(context, R.string.freezeCompleted);
                        }
                    } else {
                        showToast(context, R.string.failed);
                    }
                } else {
                    if (processRootAction(pkgName, target, tasks, context, false, askRun, false, null, false)) {
                        if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                            showToast(context, R.string.executed);
                        }
                    } else {
                        showToast(context, R.string.failed);
                    }
                }
            } else {
                if (checkMRootFrozen(context, pkgName)) {
                    if (processMRootAction(context, pkgName, target, tasks, false, askRun, runImmediately, null, false)) {
                        if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                            showToast(context, R.string.UFCompleted);
                        }
                    } else {
                        showToast(context, R.string.failed);
                    }
                } else {
                    if (processRootAction(pkgName, target, tasks, context, true, askRun, runImmediately, null, false)) {
                        if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                            showToast(context, R.string.executed);
                        }
                    } else {
                        showToast(context, R.string.failed);
                    }
                }
            }
        } else {
            String[] packages = intent.getStringArrayExtra("packages");
            if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
                oneKeyActionMRoot(context, freeze, packages);
            } else {
                oneKeyActionRoot(context, freeze, packages);
            }
        }
        stopSelf();
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
