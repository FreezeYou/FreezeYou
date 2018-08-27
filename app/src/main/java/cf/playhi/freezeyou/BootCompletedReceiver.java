package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;

import net.grandcentrix.tray.AppPreferences;

import static android.content.Context.MODE_PRIVATE;
import static cf.playhi.freezeyou.Support.checkMRootFrozen;
import static cf.playhi.freezeyou.Support.checkRootFrozen;
import static cf.playhi.freezeyou.Support.createNotification;
import static cf.playhi.freezeyou.Support.getApplicationIcon;
import static cf.playhi.freezeyou.Support.getBitmapFromDrawable;
import static cf.playhi.freezeyou.Support.publishTask;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
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
        AppPreferences defaultSharedPreferences = new AppPreferences(context);
        String string = defaultSharedPreferences.getString("notifying", "");
        if (string != null && !"".equals(string)) {
            String[] strings = string.split(",");
            PackageManager pm = context.getPackageManager();
            for (String aPkgName : strings) {
                if (!checkFrozenStatus(context, aPkgName, pm)) {
                    createNotification(context, aPkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgName, null, false)));
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
                    createNotification(context, aPkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgName, null, false)));
                }
            }
            sharedPreferences.edit().putString("notifying", "").apply();
        }
    }

    private boolean checkFrozenStatus(Context context, String packageName, PackageManager pm) {
        return (checkRootFrozen(context, packageName, pm) || checkMRootFrozen(context, packageName));
    }

    private void checkTasks(Context context) {
        SQLiteDatabase db = context.openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );

        final Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String repeat = cursor.getString(cursor.getColumnIndex("repeat"));
                int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                int minutes = cursor.getInt(cursor.getColumnIndex("minutes"));
                int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                String task = cursor.getString(cursor.getColumnIndex("task"));
                if (enabled == 1) {
                    publishTask(context, id, hour, minutes, repeat, task);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
    }
}
