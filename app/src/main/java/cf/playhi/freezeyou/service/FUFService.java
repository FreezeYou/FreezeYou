package cf.playhi.freezeyou.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import net.grandcentrix.tray.AppPreferences;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.app.FreezeYouBaseService;
import cf.playhi.freezeyou.utils.FUFUtils;

import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_LEGACY_AUTO;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_MROOT_DPM;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_ROOT_DISABLE_ENABLE;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner;
import static cf.playhi.freezeyou.utils.FUFUtils.checkMRootFrozen;
import static cf.playhi.freezeyou.utils.FUFUtils.oneKeyAction;
import static cf.playhi.freezeyou.utils.FUFUtils.processAction;
import static cf.playhi.freezeyou.utils.FUFUtils.processMRootAction;
import static cf.playhi.freezeyou.utils.FUFUtils.processRootAction;

public class FUFService extends FreezeYouBaseService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean freeze = intent.getBooleanExtra("freeze", false);
        Context context = getApplicationContext();
        AppPreferences appPreferences = new AppPreferences(context);
        int apiMode = appPreferences.getInt("selectFUFMode", 0);
        if (intent.getBooleanExtra("single", false)) {
            String pkgName = intent.getStringExtra("pkgName");
            String target = intent.getStringExtra("target");
            String tasks = intent.getStringExtra("tasks");
            boolean askRun = intent.getBooleanExtra("askRun", false);
            boolean runImmediately = intent.getBooleanExtra("runImmediately", false);
            if (apiMode == API_FREEZEYOU_LEGACY_AUTO) {
                if (freeze) {
                    if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
                        processMRootAction(
                                context, pkgName, target, tasks, true, askRun,
                                false, null, false,
                                !(appPreferences.getBoolean("lesserToast", false)));
                    } else {
                        processRootAction(pkgName, target, tasks, context, false, askRun,
                                false, null, false,
                                !(appPreferences.getBoolean("lesserToast", false)));
                    }
                } else {
                    if (checkMRootFrozen(context, pkgName)) {
                        processMRootAction(context, pkgName, target, tasks, false,
                                askRun, runImmediately, null, false,
                                !(appPreferences.getBoolean("lesserToast", false)));
                    } else {
                        processRootAction(pkgName, target, tasks, context, true, askRun,
                                runImmediately, null, false,
                                !(appPreferences.getBoolean("lesserToast", false)));
                    }
                }
            } else {
                processAction(
                        context, pkgName, apiMode, !freeze,
                        !(appPreferences.getBoolean("lesserToast", false)),
                        askRun, target, tasks,
                        runImmediately, null, false
                );
            }
        } else {
            String[] packages = intent.getStringArrayExtra("packages");

            int decidedApiMode;

            if (apiMode == API_FREEZEYOU_LEGACY_AUTO) {
                if (freeze) {
                    if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
                        decidedApiMode = API_FREEZEYOU_MROOT_DPM;
                    } else {
                        decidedApiMode = API_FREEZEYOU_ROOT_DISABLE_ENABLE;
                    }
                } else {
                    if (FUFUtils.checkRootPermission()) {
                        decidedApiMode = API_FREEZEYOU_ROOT_DISABLE_ENABLE;
                    } else {
                        decidedApiMode = API_FREEZEYOU_MROOT_DPM;
                    }
                }
            } else {
                decidedApiMode = apiMode;
            }

            oneKeyAction(context, freeze, packages, decidedApiMode);
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(
                        new NotificationChannel(
                                "FreezeAndUnfreeze", getString(R.string.freezeAUF),
                                NotificationManager.IMPORTANCE_NONE
                        )
                );
            Notification.Builder mBuilder =
                    new Notification.Builder(this, "FreezeAndUnfreeze");
            mBuilder.setSmallIcon(R.drawable.ic_notification);
            mBuilder.setContentText(getString(R.string.freezeAUF));
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
