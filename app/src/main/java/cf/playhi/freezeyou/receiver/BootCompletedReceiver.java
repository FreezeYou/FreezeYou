package cf.playhi.freezeyou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;
import java.io.IOException;

import cf.playhi.freezeyou.service.ScreenLockOneKeyFreezeService;
import cf.playhi.freezeyou.utils.FileUtils;
import cf.playhi.freezeyou.utils.ServiceUtils;
import cf.playhi.freezeyou.utils.TasksUtils;
import cf.playhi.freezeyou.utils.VersionUtils;

import static cf.playhi.freezeyou.utils.FUFUtils.checkAndCreateFUFQuickNotification;
import static cf.playhi.freezeyou.utils.FUFUtils.checkMRootFrozen;
import static cf.playhi.freezeyou.utils.FUFUtils.checkRootFrozen;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null || intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                runBackgroundService(context);
                checkAndReNotifyNotifications(context);
                checkTasks(context);
                break;
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                runBackgroundService(context);
                checkAndReNotifyNotifications(context);
                checkTasks(context);
                cleanExternalCache(context);

                final SharedPreferences sharedPreferences =
                        context.getSharedPreferences("Ver", Context.MODE_PRIVATE);
                if (sharedPreferences.getInt("Ver", 0) < VersionUtils.getVersionCode(context)) {
                    clearCrashLogs();
                }

                break;
            default:
                break;
        }
    }

    private void cleanExternalCache(Context context) {
        try {
            FileUtils.deleteAllFiles(context.getExternalCacheDir(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runBackgroundService(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("onekeyFreezeWhenLockScreen", false)) {
            ServiceUtils.startService(context,
                    new Intent(context, ScreenLockOneKeyFreezeService.class));
        }
    }

    private void checkAndReNotifyNotifications(Context context) {
        AppPreferences defaultSharedPreferences = new AppPreferences(context);
        String string = defaultSharedPreferences.getString("notifying", "");
        if (string != null && !"".equals(string)) {
            String[] strings = string.split(",");
            PackageManager pm = context.getPackageManager();
            for (String aPkgName : strings) {
                if (!checkFrozenStatus(context, aPkgName, pm)) {
                    checkAndCreateFUFQuickNotification(context, aPkgName);
                }
            }
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String oldNotifying = sharedPreferences.getString("notifying", "");
        if (oldNotifying != null && !"".equals(oldNotifying)) {
            String[] oldNotifyings = oldNotifying.split(",");
            PackageManager pm = context.getPackageManager();
            for (String aPkgName : oldNotifyings) {
                if (!checkFrozenStatus(context, aPkgName, pm)) {
                    checkAndCreateFUFQuickNotification(context, aPkgName);
                }
            }
            sharedPreferences.edit().putString("notifying", "").apply();
        }
    }

    private boolean checkFrozenStatus(Context context, String packageName, PackageManager pm) {
        return (checkRootFrozen(context, packageName, pm) || checkMRootFrozen(context, packageName));
    }

    private void checkTasks(Context context) {
        checkTimeTasks(context);
        checkTriggerTasks(context);
    }

    private void checkTimeTasks(Context context) {
        TasksUtils.checkTimeTasks(context);
    }

    private void checkTriggerTasks(Context context) {
        TasksUtils.checkTriggerTasks(context);
    }

    private void clearCrashLogs() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            String logPath =
                    Environment.getExternalStorageDirectory()
                            .getAbsolutePath()
                            + File.separator
                            + File.separator
                            + "FreezeYou"
                            + File.separator
                            + "Log";
            try {
                FileUtils.deleteAllFiles(new File(logPath), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String logPath2 =
                Environment.getDataDirectory().getPath()
                        + File.separator
                        + "data"
                        + File.separator
                        + "cf.playhi.freezeyou"
                        + File.separator
                        + "log";
        try {
            FileUtils.deleteAllFiles(new File(logPath2), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File crashCheck =
                new File(logPath2
                        + File.separator
                        + "NeedUpload.log");
        if (crashCheck.exists()) {
            crashCheck.delete();
        }
    }
}
