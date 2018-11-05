package cf.playhi.freezeyou;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityEvent;

import net.grandcentrix.tray.AppPreferences;

import java.util.Arrays;

import static cf.playhi.freezeyou.Support.existsInOneKeyList;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    @SuppressLint("SwitchIntDef")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (accessibilityEvent.isFullScreen()) {
                    CharSequence pkgName = accessibilityEvent.getPackageName();
                    if (pkgName != null) {
                        boolean isScreenOn = true;
                        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                        if (pm != null) {
                            isScreenOn = pm.isScreenOn();
                        }
                        String pkgNameString = pkgName.toString();
                        String previousPkg = MainApplication.getCurrentPackage();
                        if (isScreenOn &&
                                !"".equals(pkgNameString) &&
                                !"com.android.systemui".equals(pkgNameString) &&
                                !"com.android.packageinstaller".equals(pkgNameString) &&
                                !"android".equals(pkgNameString)) {
                            MainApplication.setCurrentPackage(pkgNameString);
                            if (!pkgNameString.equals(previousPkg)
                                    && new AppPreferences(getApplicationContext()).getBoolean("freezeOnceQuit", false)
                                    && existsInOneKeyList(getApplicationContext(), getString(R.string.sFreezeOnceQuit), previousPkg)) {
                                Support.processFreezeAction(getApplicationContext(), previousPkg, false, null, false);
                            }

                            onLeaveApplications(previousPkg, pkgNameString);//检测+执行
                        }

                        onApplicationsForeground(previousPkg, pkgNameString);//检测+执行

                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void onApplicationsForeground(String previousPkg, @NonNull String pkgNameString) {

        if (!pkgNameString.equals(previousPkg) && !"cf.playhi.freezeyou".equals(previousPkg)) {
            final SQLiteDatabase db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
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
                    if (enabled == 1 && "onApplicationsForeground".equals(tg) && ("".equals(tgExtra) || Arrays.asList(tgExtra.split(",")).contains(pkgNameString))) {
                        String task = cursor.getString(cursor.getColumnIndex("task"));
                        if (task != null && !"".equals(task)) {
                            TasksUtils.runTask(task.toLowerCase(), this, null);
                        }
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            db.close();
        }
    }

    private void onLeaveApplications(String previousPkg, @NonNull String pkgNameString) {

        if (!pkgNameString.equals(previousPkg) && !"cf.playhi.freezeyou".equals(previousPkg)) {
            final SQLiteDatabase db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
            db.execSQL(
                    "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
            );
            Cursor cursor = db.query("tasks", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String tg = cursor.getString(cursor.getColumnIndex("tg"));
                    int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                    String tgExtra = cursor.getString(cursor.getColumnIndex("tgextra"));
                    if (tgExtra == null) {
                        tgExtra = "";
                    }
                    if (enabled == 1 && "onLeaveApplications".equals(tg) && ("".equals(tgExtra) || Arrays.asList(tgExtra.split(",")).contains(previousPkg))) {
                        String task = cursor.getString(cursor.getColumnIndex("task"));
                        if (task != null && !"".equals(task)) {
                            TasksUtils.runTask(task.toLowerCase(), this, null);
                        }
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            db.close();
        }
    }
}
