package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import static cf.playhi.freezeyou.Support.checkMRootFrozen;
import static cf.playhi.freezeyou.Support.checkRootFrozen;
import static cf.playhi.freezeyou.Support.createNotification;
import static cf.playhi.freezeyou.Support.getApplicationIcon;
import static cf.playhi.freezeyou.Support.getBitmapFromDrawable;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case Intent.ACTION_BOOT_COMPLETED:
                    runBackgroundService(context);
                    checkAndReNotifyNotifications(context);
                    break;
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                    runBackgroundService(context);
                    checkAndReNotifyNotifications(context);
                    break;
                default:
                    break;
            }
        }
    }

    private void runBackgroundService(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("onekeyFreezeWhenLockScreen", false)) {
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(new Intent(context, ScreenLockOneKeyFreezeService.class));
            } else {
                context.startService(new Intent(context, ScreenLockOneKeyFreezeService.class));
            }
        }
    }

    private void checkAndReNotifyNotifications(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String string = defaultSharedPreferences.getString("notifying", "");
        if (!"".equals(string)) {
            String[] strings = string.split(",");
            PackageManager pm = context.getPackageManager();
            for (String aPkgName : strings) {
                if (!checkFrozenStatus(context, aPkgName, pm)) {
                    createNotification(context, aPkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgName, null, false)));
                }
            }
        }
    }

    private boolean checkFrozenStatus(Context context, String packageName, PackageManager pm) {
        return (checkRootFrozen(context, packageName, pm) || checkMRootFrozen(context, packageName));
    }
}
