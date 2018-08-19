package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;

public class TasksNeedExecuteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -5);
        int hour = intent.getIntExtra("hour", -1);
        int minute = intent.getIntExtra("minute", -1);
        String task = intent.getStringExtra("task");
        String repeat = intent.getStringExtra("repeat");
        SQLiteDatabase db = context.openOrCreateDatabase("scheduledTasks", Context.MODE_PRIVATE, null);
        if ("0".equals(repeat) && id != -5) {
            db.execSQL("UPDATE tasks SET enabled = 0 WHERE _id = " + Integer.toString(id) + ";");
        } else {
            Support.publishTask(context, id, hour, minute, repeat, task);
        }
        if (task != null && !"".equals(task)) {
            runTask(task.toLowerCase(), context);//全部转小写
        }
    }

    private void runTask(@NonNull String task, Context context) {
        if ("okff".equals(task)) {
            startService(context, new Intent(context, OneKeyFreezeService.class).putExtra("autoCheckAndLockScreen", false));
        } else if ("okuf".equals(task)) {
            startService(context, new Intent(context, OneKeyUFService.class));
        } else if (task.length() >= 4) {
            String string = task.substring(0, 2);
            String[] tasks = task.substring(3).replaceAll(" ", "").split(",");
            switch (string) {
                case "ff":
                    startService(
                            context,
                            new Intent(context, FUFService.class)
                                    .putExtra("packages", tasks)
                                    .putExtra("freeze", true)
                    );
                    break;
                case "uf":
                    startService(
                            context,
                            new Intent(context, FUFService.class)
                                    .putExtra("packages", tasks)
                                    .putExtra("freeze", false)
                    );
                    break;
                case "es": //enableSettings
                    enableAndDisableSysSettings(tasks, context, true);
                    break;
                case "ds": //disableSettings
                    enableAndDisableSysSettings(tasks, context, false);
                    break;
                default:
                    break;
            }
        }
    }

    private void enableAndDisableSysSettings(String[] tasks, Context context, boolean enable) {
        for (String aTask : tasks) {
            switch (aTask) {
                case "wifi"://WiFi
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager != null)
                        wifiManager.setWifiEnabled(enable);
                    break;
                default:
                    break;
            }
        }
    }

    private void startService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
