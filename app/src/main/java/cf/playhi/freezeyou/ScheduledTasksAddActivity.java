package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.HashSet;
import java.util.Set;

import static cf.playhi.freezeyou.TasksUtils.publishTask;
import static cf.playhi.freezeyou.ThemeUtils.getThemeDot;
import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.ToastUtils.showToast;

public class ScheduledTasksAddActivity extends Activity {

    private boolean isTimeTask;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stma_add);
        id = getIntent().getIntExtra("id", -5);
        isTimeTask = getIntent().getBooleanExtra("time", true);
        ActionBar actionBar = getActionBar();
        processActionBar(actionBar);
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("label"));
        }
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.staa_menu, menu);
        String cTheme = ThemeUtils.getUiTheme(ScheduledTasksAddActivity.this);
        if ("white".equals(cTheme) || "default".equals(cTheme))
            menu.findItem(R.id.menu_staa_delete).setIcon(R.drawable.ic_action_delete_light);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                checkAndDecideIfFinish();
                return true;
            case R.id.menu_staa_delete:
                AlertDialogUtils.buildAlertDialog(this, android.R.drawable.ic_dialog_alert, R.string.askIfDel, R.string.notice)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_OK);
                                if (id != -5) {
                                    SQLiteDatabase db = openOrCreateDatabase(isTimeTask ? "scheduledTasks" : "scheduledTriggerTasks", MODE_PRIVATE, null);
                                    if (isTimeTask) {
                                        TasksUtils.cancelTheTask(ScheduledTasksAddActivity.this, id);
                                    }
                                    db.execSQL("DELETE FROM tasks WHERE _id = " + id);
                                    db.close();
                                    finish();
                                } else {
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            checkAndDecideIfFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void init() {

        prepareData(id);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.staa_sp, isTimeTask ? new STAAFragment() : new STAATriggerFragment())
                .commit();

        prepareSaveButton(id);
    }

    private void prepareData(int id) {
        if (id != -5) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ScheduledTasksAddActivity.this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isTimeTask) {
                final SQLiteDatabase db = openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
                db.execSQL(
                        "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
                );
                Cursor cursor = db.query("tasks", null, "_id=?", new String[]{Integer.toString(id)}, null, null, null);
                if (cursor.moveToFirst()) {
                    editor.putString("stma_add_time", Integer.toString(cursor.getInt(cursor.getColumnIndex("hour"))) + ":" + Integer.toString(cursor.getInt(cursor.getColumnIndex("minutes"))));
                    editor.putBoolean("stma_add_enable", cursor.getInt(cursor.getColumnIndex("enabled")) == 1);
                    editor.putString("stma_add_label", cursor.getString(cursor.getColumnIndex("label")));
                    editor.putString("stma_add_task", cursor.getString(cursor.getColumnIndex("task")));
                    HashSet<String> hashSet = new HashSet<>();
                    String repeat = cursor.getString(cursor.getColumnIndex("repeat"));
                    for (int i = 0; i < repeat.length(); i++) {
                        hashSet.add(repeat.substring(i, i + 1));
                    }
                    editor.putStringSet("stma_add_repeat", hashSet);
                }
                cursor.close();
                db.close();
            } else {
                final SQLiteDatabase db = ScheduledTasksAddActivity.this.openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
                db.execSQL(
                        "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
                );
                Cursor cursor = db.query("tasks", null, "_id=?", new String[]{Integer.toString(id)}, null, null, null);
                if (cursor.moveToFirst()) {
                    editor.putString("stma_add_trigger_extra_parameters", cursor.getString(cursor.getColumnIndex("tgextra")));
                    editor.putBoolean("stma_add_enable", cursor.getInt(cursor.getColumnIndex("enabled")) == 1);
                    editor.putString("stma_add_label", cursor.getString(cursor.getColumnIndex("label")));
                    editor.putString("stma_add_task", cursor.getString(cursor.getColumnIndex("task")));
                    editor.putString("stma_add_trigger", cursor.getString(cursor.getColumnIndex("tg")));
                }
                cursor.close();
                db.close();
            }
            editor.apply();
        }
    }

    private void prepareSaveButton(final int id) {
        final ImageButton saveButton = findViewById(R.id.staa_saveButton);
        saveButton.setBackgroundResource(Build.VERSION.SDK_INT < 21 ? getThemeDot(ScheduledTasksAddActivity.this) : R.drawable.oval_ripple);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (isTimeTask) {
                    if (saveTimeTaskData(defaultSharedPreferences, id)) {
                        finish();
                    }
                } else {
                    if (saveTriggerTaskData(defaultSharedPreferences, id)) {
                        finish();
                    }
                }
            }
        });
    }

    private boolean saveTimeTaskData(SharedPreferences defaultSharedPreferences, int id) {
        String time = defaultSharedPreferences.getString("stma_add_time", "09:09");
        if (time == null) {
            time = "09:09";
        }
        int indexOfColon = time.indexOf(":");
        if (indexOfColon == -1) {
            showToast(this, R.string.mustContainColon);
            return false;
        }
        int hour;
        int minutes;
        try {
            hour = Integer.valueOf(time.substring(0, indexOfColon));
            minutes = Integer.valueOf(time.substring(indexOfColon + 1));
        } catch (Exception e) {
            showToast(this,
                    getString(R.string.minutesShouldBetween)
                            + System.getProperty("line.separator")
                            + getString(R.string.hourShouldBetween));
            return false;
        }
        int enabled = defaultSharedPreferences.getBoolean("stma_add_enable", true) ? 1 : 0;
        StringBuilder repeatStringBuilder = new StringBuilder();
        Set<String> stringSet = defaultSharedPreferences.getStringSet("stma_add_repeat", null);
        if (stringSet != null) {
            for (String str : stringSet) {
                switch (str) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                    case "7":
                        repeatStringBuilder.append(str);
                        break;
                    default:
                        break;
                }
            }
        }
        String repeat = repeatStringBuilder.toString().equals("") ? "0" : repeatStringBuilder.toString();
        String label = defaultSharedPreferences.getString("stma_add_label", getString(R.string.label));
        String task = defaultSharedPreferences.getString("stma_add_task", "okuf");
        SQLiteDatabase db = openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );//column1\2 留作备用
        if (id == -5) {
            db.execSQL(
                    "insert into tasks(_id,hour,minutes,repeat,enabled,label,task,column1,column2) values(null,"
                            + hour + ","
                            + minutes + ","
                            + repeat + ","
                            + enabled + ","
                            + "'" + label + "'" + ","
                            + "'" + task + "'" + ",'','')"
            );
        } else {
            db.execSQL("UPDATE tasks SET hour = "
                    + hour + ", minutes = "
                    + minutes + ", repeat = "
                    + repeat + ", enabled = "
                    + enabled + ", label = '"
                    + label + "', task = '"
                    + task + "' WHERE _id = " + Integer.toString(id) + ";");
        }
        db.close();
        TasksUtils.cancelTheTask(ScheduledTasksAddActivity.this, id);
        if (enabled == 1) {
            publishTask(ScheduledTasksAddActivity.this, id, hour, minutes, repeat, task);
        }
        setResult(RESULT_OK);
        return true;
    }

    private boolean saveTriggerTaskData(SharedPreferences defaultSharedPreferences, int id) {
        String triggerExtraParameters = defaultSharedPreferences.getString("stma_add_trigger_extra_parameters", "");
        int enabled = defaultSharedPreferences.getBoolean("stma_add_enable", true) ? 1 : 0;
        String label = defaultSharedPreferences.getString("stma_add_label", getString(R.string.label));
        String task = defaultSharedPreferences.getString("stma_add_task", "okuf");
        String trigger = defaultSharedPreferences.getString("stma_add_trigger", "");
        if ("".equals(trigger)) {//未指定触发器，直接return，抛failed
            showToast(this, R.string.failed);
            return false;
        }
        SQLiteDatabase db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        db.execSQL("replace into tasks(_id,tg,tgextra,enabled,label,task,column1,column2) VALUES ( "
                + ((id == -5) ? null : id) + ",'"
                + trigger + "','" + triggerExtraParameters + "'," + enabled + ",'" + label + "','" + task + "','','')");
        db.close();
        if (enabled == 1 && trigger != null) {
            switch (trigger) {
                case "onScreenOn":
                    ServiceUtils.startService(this,
                            new Intent(this, TriggerTasksService.class)
                                    .putExtra("OnScreenOn", true));
                    break;
                case "onScreenOff":
                    ServiceUtils.startService(this,
                            new Intent(this, TriggerTasksService.class)
                                    .putExtra("OnScreenOff", true));
                    break;
                case "onApplicationsForeground":
                case "onLeaveApplications":
                    if (!AccessibilityUtils.isAccessibilitySettingsOn(this)) {
                        showToast(this, R.string.needActiveAccessibilityService);
                        AccessibilityUtils.openAccessibilitySettings(this);
                    }
                    break;
                default:
                    break;
            }
        }
        setResult(RESULT_OK);
        return true;
    }

    private void checkAndDecideIfFinish() {
        AlertDialogUtils.buildAlertDialog(this, R.mipmap.ic_launcher_new_round, R.string.askIfSave, R.string.notice)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isTimeTask) {
                            if (saveTimeTaskData(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), id)) {
                                finish();
                            }
                        } else {
                            if (saveTriggerTaskData(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), id)) {
                                finish();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .create().show();
    }
}
