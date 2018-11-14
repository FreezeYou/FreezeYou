package cf.playhi.freezeyou;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.grandcentrix.tray.AppPreferences;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static cf.playhi.freezeyou.ProcessUtils.destroyProcess;
import static cf.playhi.freezeyou.ServiceUtils.startService;
import static cf.playhi.freezeyou.ToastUtils.showToast;

final class TasksUtils {

    static void publishTask(Context context, int id, int hour, int minute, String repeat, String task) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TasksNeedExecuteReceiver.class)
                .putExtra("id", id)
                .putExtra("task", task)
                .putExtra("repeat", repeat)
                .putExtra("hour", hour)
                .putExtra("minute", minute);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        long systemTime = System.currentTimeMillis();
        calendar.setTimeInMillis(systemTime);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (alarmMgr != null) {
            if ("0".equals(repeat)) {
                if (systemTime >= calendar.getTimeInMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                setTask(alarmMgr, calendar.getTimeInMillis(), alarmIntent);
            } else {
                long timeInterval = Long.MAX_VALUE;
                long timeTmp;
                for (int i = 0; i < repeat.length(); i++) {
                    switch (repeat.substring(i, i + 1)) {
                        case "1":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                            break;
                        case "2":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            break;
                        case "3":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                            break;
                        case "4":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                            break;
                        case "5":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                            break;
                        case "6":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                            break;
                        case "7":
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                            break;
                        default:
                            break;
                    }
                    timeTmp = calculateTimeInterval(systemTime, calendar.getTimeInMillis());
                    if (timeTmp <= 0) {
                        timeTmp = timeTmp + 604800000;
                    }
                    if (timeTmp > 0 && timeTmp < timeInterval) {
                        timeInterval = timeTmp;
                    }
                }
                setTask(alarmMgr, systemTime + timeInterval, alarmIntent);
            }
        } else {
            showToast(context, R.string.requestFailedPlsRetry);
        }
    }

    private static void setTask(AlarmManager alarmManager, long triggerAtMillis, PendingIntent operation) {//RTC
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        }
    }

    private static void setRealTimeTask(AlarmManager alarmManager, long triggerAtMillis, PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, operation);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, operation);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, operation);
        }
    }

    private static long calculateTimeInterval(long first, long last) {
        return last - first;
    }

    static void runTask(String task, Context context, String taskTrigger) {
        String[] sTasks = task.split(";");
        for (String asTasks : sTasks) {
            if (asTasks.startsWith("okff")) {
                if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                    startService(context, new Intent(context, OneKeyFreezeService.class).putExtra("autoCheckAndLockScreen", false));
            } else if (asTasks.startsWith("okuf")) {
                if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                    startService(context, new Intent(context, OneKeyUFService.class));
            } else if (asTasks.length() >= 4) {
                String string = asTasks.substring(0, 2);
                String[] tasks = asTasks.substring(3).split(",");
                switch (string) {
                    case "ff":
                        if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                            startService(
                                    context,
                                    new Intent(context, FUFService.class)
                                            .putExtra("packages", tasks)
                                            .putExtra("freeze", true)
                            );
                        break;
                    case "uf":
                        if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                            startService(
                                    context,
                                    new Intent(context, FUFService.class)
                                            .putExtra("packages", tasks)
                                            .putExtra("freeze", false)
                            );
                        break;
                    case "es": //enableSettings
                        if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                            enableAndDisableSysSettings(tasks, context, true);
                        break;
                    case "ds": //disableSettings
                        if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                            enableAndDisableSysSettings(tasks, context, false);
                        break;
                    case "st"://showToast
                        if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                            showToast(context, asTasks.substring(3));
                        break;
                    case "lg"://LOG.E
                        if (parseTaskAndReturnIfNeedExecuteImmediately(context, asTasks, taskTrigger))
                            Log.e("TasksLogE", asTasks.substring(3));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static void enableAndDisableSysSettings(String[] tasks, Context context, boolean enable) {
        for (String aTask : tasks) {
            switch (aTask) {
                case "wifi"://WiFi
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager != null)
                        wifiManager.setWifiEnabled(enable);
                    break;
                case "cd"://CellularData
                    setMobileDataEnabled(context, enable);
                    break;
                case "bluetooth"://Bluetooth
                    if (enable) {
                        BluetoothAdapter.getDefaultAdapter().enable();
                    } else {
                        BluetoothAdapter.getDefaultAdapter().disable();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static boolean parseTaskAndReturnIfNeedExecuteImmediately(Context context, String task, String taskTrigger) {
        String[] splitTask = task.split(" ");
        int splitTaskLength = splitTask.length;
        for (int i = 0; i < splitTaskLength; i++) {
            switch (splitTask[i]) {
                case "-d":
                    if (splitTaskLength >= i + 1) {
                        long delayAtSeconds = Long.valueOf(splitTask[i + 1]);
                        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(context, TasksNeedExecuteReceiver.class)
                                .putExtra("id", -6)
                                .putExtra("task", task.replace(" -d " + splitTask[i + 1], ""))
                                .putExtra("repeat", "-1")
                                .putExtra("hour", -1)
                                .putExtra("minute", -1);
                        int requestCode = (task + new Date().toString()).hashCode();
                        PendingIntent pendingIntent =
                                PendingIntent.getBroadcast(
                                        context,
                                        requestCode,
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
                        createDelayTasks(alarmMgr, delayAtSeconds, pendingIntent);
                        if (taskTrigger != null) {//定时或无撤回判断能力或目前不计划实现撤销的任务直接null
                            AppPreferences appPreferences = new AppPreferences(context);
                            appPreferences.put(taskTrigger, appPreferences.getString(taskTrigger, "") + Integer.toString(requestCode) + ",");
                        }
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private static void createDelayTasks(AlarmManager alarmManager, long delayAtSeconds, PendingIntent pendingIntent) {

        setRealTimeTask(alarmManager, SystemClock.elapsedRealtime() + delayAtSeconds * 1000, pendingIntent);

    }

    static void onUFApplications(Context context, String pkgNameString) {
        final SQLiteDatabase db = context.openOrCreateDatabase("scheduledTriggerTasks", Context.MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String tgExtra = cursor.getString(cursor.getColumnIndex("tgextra"));
                if (tgExtra == null) {
                    tgExtra = "";
                }
                String tg = cursor.getString(cursor.getColumnIndex("tg"));
                int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                if (enabled == 1 && "onUFApplications".equals(tg) && ("".equals(tgExtra) || Arrays.asList(tgExtra.split(",")).contains(pkgNameString))) {
                    String task = cursor.getString(cursor.getColumnIndex("task"));
                    if (task != null && !"".equals(task)) {
                        runTask(task.replace("[cpkgn]", pkgNameString), context, null);
                    }
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
    }

    private static void setMobileDataEnabled(Context context, boolean enable) {
        //https://stackoverflow.com/questions/21511216/toggle-mobile-data-programmatically-on-android-4-4-2
        try {//4.4及以下
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class aClass = mConnectivityManager.getClass();
            Class[] argsClass = new Class[1];
            argsClass[0] = boolean.class;
            Method method = aClass.getMethod("setMobileDataEnabled", argsClass);
            method.invoke(mConnectivityManager, enable);
        } catch (Exception e) {
            e.printStackTrace();
            try {//pri-app方法
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Method methodSet = Class.forName(tm.getClass().getName()).getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                methodSet.invoke(tm, true);
            } catch (Exception ee) {
                ee.printStackTrace();
                try {//Root方法
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
                    outputStream.writeBytes("svc data " + (enable ? "enable" : "disable") + "\n");
                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    process.waitFor();
                    destroyProcess(outputStream, process);
                } catch (Exception eee) {//暂时无计可施……
                    eee.printStackTrace();
                    showToast(context, R.string.failed);
                }
            }
        }
    }

    static void cancelAllUnexecutedDelayTasks(Context context, String typeNeedsCheckTaskTrigger) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TasksNeedExecuteReceiver.class);
        AppPreferences appPreferences = new AppPreferences(context);
        String unprocessed = appPreferences.getString(typeNeedsCheckTaskTrigger, "");
        if (unprocessed == null)
            unprocessed = "";

        for (String id : unprocessed.split(",")) {
            if (id != null && !"".equals(id)) {
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                if (alarmMgr != null) {
                    alarmMgr.cancel(alarmIntent);
                }
            }
        }
        appPreferences.put(typeNeedsCheckTaskTrigger, "");
    }

}
