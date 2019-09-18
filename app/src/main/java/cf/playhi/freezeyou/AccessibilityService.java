package cf.playhi.freezeyou;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;

import net.grandcentrix.tray.AppPreferences;

import java.util.Arrays;

import cf.playhi.freezeyou.utils.DataStatisticsUtils;
import cf.playhi.freezeyou.utils.FUFUtils;
import cf.playhi.freezeyou.utils.OneKeyListUtils;
import cf.playhi.freezeyou.utils.TasksUtils;

import static cf.playhi.freezeyou.utils.TasksUtils.cancelAllUnexecutedDelayTasks;

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
                                    && OneKeyListUtils.existsInOneKeyList(getApplicationContext(), getString(R.string.sFreezeOnceQuit), previousPkg)) {
                                FUFUtils.processFreezeAction(getApplicationContext(), previousPkg, null, null, false, null, false);
                            }

                            onLeaveApplications(previousPkg, pkgNameString);//检测+执行
                        }

                        onApplicationsForeground(previousPkg, pkgNameString);//检测+执行

                        addUpUseTimes(pkgNameString);//使用次数计数（增加）

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

    private void onApplicationsForeground(String previousPkg, String pkgNameString) {

        if (!pkgNameString.equals(previousPkg) && !"cf.playhi.freezeyou".equals(previousPkg)) {
            cancelAllUnexecutedDelayTasks(this, "OSA_" + previousPkg);//撤销全部属于上一应用的未执行的打开应用时
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
                    if (enabled == 1 && "onApplicationsForeground".equals(tg) && ("".equals(tgExtra) || Arrays.asList(TasksUtils.decodeUserListsInPackageNames(this, tgExtra).split(",")).contains(pkgNameString))) {
                        String task = cursor.getString(cursor.getColumnIndex("task"));
                        if (task != null && !"".equals(task)) {
                            TasksUtils.runTask(
                                    task.replace("[ppkgn]", previousPkg).replace("[cpkgn]", pkgNameString),
                                    this,
                                    ("".equals(tgExtra) ? null : "OSA_" + pkgNameString)
                            );
                        }
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            db.close();
        }
    }

    private void onLeaveApplications(String previousPkg, String pkgNameString) {

        if (!pkgNameString.equals(previousPkg) && !"cf.playhi.freezeyou".equals(previousPkg)) {
            cancelAllUnexecutedDelayTasks(this, "OLA_" + pkgNameString);//撤销全部属于被打开应用的未执行的离开应用时
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
                    if (enabled == 1 && "onLeaveApplications".equals(tg) && ("".equals(tgExtra) || Arrays.asList(TasksUtils.decodeUserListsInPackageNames(this, tgExtra).split(",")).contains(previousPkg))) {
                        String task = cursor.getString(cursor.getColumnIndex("task"));
                        if (task != null && !"".equals(task)) {
                            TasksUtils.runTask(
                                    task.replace("[ppkgn]", previousPkg).replace("[cpkgn]", pkgNameString),
                                    this,
                                    "".equals(tgExtra) ? null : "OLA_" + previousPkg
                            );
                        }
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            db.close();
        }
    }

    private void addUpUseTimes(String currentPackage) {
        DataStatisticsUtils.addUseTimes(getApplicationContext(), currentPackage);
    }
}
