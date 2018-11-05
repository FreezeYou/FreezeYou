package cf.playhi.freezeyou;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;

import net.grandcentrix.tray.AppPreferences;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class TriggerTasksService extends Service {

    private TriggerScreenLockListener triggerScreenLockListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (triggerScreenLockListener == null && intent.getBooleanExtra("OnScreenOn", false)) {
            triggerScreenLockListener = new TriggerScreenLockListener(getApplicationContext());
            triggerScreenLockListener.registerListener();
        }

        if (triggerScreenLockListener == null && intent.getBooleanExtra("OnScreenOff", false)) {
            triggerScreenLockListener = new TriggerScreenLockListener(getApplicationContext());
            triggerScreenLockListener.registerListener();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (new AppPreferences(getApplicationContext()).getBoolean("useForegroundService", false) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder mBuilder = new Notification.Builder(this);
                mBuilder.setSmallIcon(R.drawable.ic_notification);
                mBuilder.setContentText(getString(R.string.backgroundService));
                NotificationChannel channel = new NotificationChannel("BackgroundService", getString(R.string.backgroundService), NotificationManager.IMPORTANCE_NONE);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
                mBuilder.setChannelId("BackgroundService");
                Intent resultIntent = new Intent(getApplicationContext(), Main.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                startForeground(1, mBuilder.build());
            } else {
                startForeground(1, new Notification());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        if (triggerScreenLockListener != null) {
            triggerScreenLockListener.unregisterListener();
            triggerScreenLockListener = null;
        }
        stopForeground(true);
        super.onDestroy();
    }
}

class TriggerScreenLockListener {

    private final Context mContext;
    private final ScreenLockBroadcastReceiver mScreenLockReceiver;

    TriggerScreenLockListener(Context context) {
        mContext = context;
        mScreenLockReceiver = new ScreenLockBroadcastReceiver();
    }

    private class ScreenLockBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Cursor cursor = getCursor(context);
            if (action != null && cursor.moveToFirst()) {
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
                        cancelAllUnexecutedDelayTasks(context, "onScreenOn");
                        for (int i = 0; i < cursor.getCount(); i++) {
                            String tg = cursor.getString(cursor.getColumnIndex("tg"));
                            int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                            if (enabled == 1 && "onScreenOff".equals(tg)) {
                                String task = cursor.getString(cursor.getColumnIndex("task"));
                                if (task != null && !"".equals(task)) {
                                    TasksUtils.runTask(task.toLowerCase(), context, "onScreenOff");
                                }
                            }
                            cursor.moveToNext();
                        }
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        cancelAllUnexecutedDelayTasks(context, "onScreenOff");
                        for (int i = 0; i < cursor.getCount(); i++) {
                            String tg = cursor.getString(cursor.getColumnIndex("tg"));
                            int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                            if (enabled == 1 && "onScreenOn".equals(tg)) {
                                String task = cursor.getString(cursor.getColumnIndex("task"));
                                if (task != null && !"".equals(task)) {
                                    TasksUtils.runTask(task.toLowerCase(), context, "onScreenOn");
                                }
                            }
                            cursor.moveToNext();
                        }
                        break;
                    default:
                        break;
                }
            }
            cursor.close();
        }
    }

    void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mScreenLockReceiver, filter);
    }

    void unregisterListener() {
        mContext.unregisterReceiver(mScreenLockReceiver);
    }

    private Cursor getCursor(Context context) {
        final SQLiteDatabase db = context.openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );

        return db.query("tasks", null, null, null, null, null, null);
    }

    private void cancelAllUnexecutedDelayTasks(Context context, String typeNeedsCheck) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, TasksNeedExecuteReceiver.class);
        AppPreferences appPreferences = new AppPreferences(context);
        String unprocessed = appPreferences.getString(typeNeedsCheck, "");
        if (unprocessed == null)
            unprocessed = "";

        for (String id : unprocessed.split(",")) {
            if (id!=null&&!"".equals(id)){
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                if (alarmMgr != null) {
                    alarmMgr.cancel(alarmIntent);
                }
            }
        }
        appPreferences.put(typeNeedsCheck, "");
    }
}
