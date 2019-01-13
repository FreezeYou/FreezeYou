package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cf.playhi.freezeyou.ThemeUtils.getThemeDot;
import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.ToastUtils.showToast;

public class ScheduledTasksManageActivity extends Activity {

    private final ArrayList<Integer> integerArrayList = new ArrayList<>();
    private final ArrayList<Integer> selectedTasksPositions = new ArrayList<>();
    private int themeDotResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stma_main);
        processActionBar(getActionBar());
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Build.VERSION.SDK_INT >= 21)
                    finishAfterTransition();
                else
                    finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (findViewById(R.id.stma_addTimeButton).getVisibility() == View.VISIBLE)
                    changeFloatButtonsStatus(false);
                if (resultCode == RESULT_OK)
                    updateTasksList();
                break;
            case 2:
                if (findViewById(R.id.stma_addTriggerButton).getVisibility() == View.VISIBLE)
                    changeFloatButtonsStatus(false);
                if (resultCode == RESULT_OK)
                    updateTasksList();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init() {
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(this, ScheduledTasksManageActivity.class));
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.scheduledTasks));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher_new_round));
            setResult(RESULT_OK, intent);
            finish();
        } else {
            generateTasksList();
            processAddButton();
        }
    }

    private void generateTasksList() {
        themeDotResId = getThemeDot(ScheduledTasksManageActivity.this);
        final ListView tasksListView = findViewById(R.id.stma_tasksListview);
        final ArrayList<Map<String, Object>> tasksData = new ArrayList<>();

        generateTimeTaskList(integerArrayList, tasksData);
        generateTriggerTaskList(integerArrayList, tasksData);

        ScheduledTasksManageSimpleAdapter adapter =
                new ScheduledTasksManageSimpleAdapter(this, tasksData, R.layout.stma_item, new String[]{"label",
                        "time", "enabled"}, new int[]{R.id.stma_label, R.id.stma_time, R.id.stma_status});

        tasksListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selectedTasksPositions.add(position);
                    mode.setTitle(Integer.toString(selectedTasksPositions.size()));
                } else {
                    selectedTasksPositions.remove(Integer.valueOf(position));
                    mode.setTitle(Integer.toString(selectedTasksPositions.size()));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                ScheduledTasksManageActivity.this.getMenuInflater().inflate(R.menu.stma_multichoicemenu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.stma_menu_mc_delete:
                        for (int aSelectedTaskPosition : selectedTasksPositions) {
                            boolean isTimeTask = (boolean) ((Map<String, Object>) tasksListView.getItemAtPosition(aSelectedTaskPosition)).get("isTimeTask");
                            int id = integerArrayList.get(aSelectedTaskPosition);
                            SQLiteDatabase db = openOrCreateDatabase(isTimeTask ? "scheduledTasks" : "scheduledTriggerTasks", MODE_PRIVATE, null);
                            if (isTimeTask) {
                                TasksUtils.cancelTheTask(ScheduledTasksManageActivity.this, id);
                            }
                            db.execSQL("DELETE FROM tasks WHERE _id = " + id);
                            db.close();
                        }
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selectedTasksPositions.clear();
                updateTasksList();
            }
        });

        tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> map = (Map<String, Object>) tasksListView.getItemAtPosition(i);
                final String label = (String) map.get("label");
                final String s = ((String) map.get("time"));
                final boolean isTimeTask = ((s != null) && s.contains(":"));
                if (Build.VERSION.SDK_INT >= 21) {
                    view.setTransitionName("add");
                    startActivityForResult(
                            new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                    .putExtra("label", label)
                                    .putExtra("time", isTimeTask)
                                    .putExtra("id", integerArrayList.get(i)),
                            isTimeTask ? 1 : 2,
                            ActivityOptions
                                    .makeSceneTransitionAnimation(
                                            ScheduledTasksManageActivity.this, view, "add")
                                    .toBundle());
                } else {
                    startActivityForResult(
                            new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                    .putExtra("label", label)
                                    .putExtra("time", isTimeTask)
                                    .putExtra("id", integerArrayList.get(i)),
                            isTimeTask ? 1 : 2);
                }
            }
        });
        tasksListView.setAdapter(adapter);
    }

    private void processAddButton() {
        final ImageButton addButton = findViewById(R.id.stma_addButton);
        final ImageButton addTimeButton = findViewById(R.id.stma_addTimeButton);
        final ImageButton addTriggerButton = findViewById(R.id.stma_addTriggerButton);

        if (Build.VERSION.SDK_INT >= 21) {
            addButton.setBackgroundResource(R.drawable.oval_ripple);
            addTriggerButton.setBackgroundResource(R.drawable.oval_ripple_almost_white);
            addTimeButton.setBackgroundResource(R.drawable.oval_ripple_almost_white);
        } else {
            addButton.setBackgroundResource(themeDotResId);
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFloatButtonsStatus(addTimeButton.getVisibility() != View.VISIBLE);
            }
        });

        addTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21)
                    startActivityForResult(
                            new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                    .putExtra("label", getString(R.string.add))
                                    .putExtra("time", true),
                            1,
                            ActivityOptions
                                    .makeSceneTransitionAnimation(
                                            ScheduledTasksManageActivity.this, addTimeButton, "add")
                                    .toBundle());
                else {
                    changeFloatButtonsStatus(false);
                    startActivityForResult(
                            new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                    .putExtra("label", getString(R.string.add))
                                    .putExtra("time", true),
                            1);
                }
            }
        });

        addTriggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21) {
                    startActivityForResult(
                            new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                    .putExtra("label", getString(R.string.add))
                                    .putExtra("time", false),
                            2,
                            ActivityOptions
                                    .makeSceneTransitionAnimation(
                                            ScheduledTasksManageActivity.this, addTriggerButton, "add")
                                    .toBundle());
                } else {
                    changeFloatButtonsStatus(false);
                    startActivityForResult(
                            new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                    .putExtra("label", getString(R.string.add))
                                    .putExtra("time", false),
                            2);
                }
            }
        });
    }

    private void changeFloatButtonsStatus(boolean showSmallButton) {
        final ImageButton addButton = findViewById(R.id.stma_addButton);
        final ImageButton addTimeButton = findViewById(R.id.stma_addTimeButton);
        final ImageButton addTriggerButton = findViewById(R.id.stma_addTriggerButton);
        if (showSmallButton) {
            RotateAnimation animation =
                    new RotateAnimation(0, 45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(300);
            animation.setRepeatMode(RotateAnimation.REVERSE);
            animation.setFillAfter(true);
            addButton.startAnimation(animation);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);
            alphaAnimation.setDuration(150);
            addTimeButton.startAnimation(alphaAnimation);
            addTriggerButton.startAnimation(alphaAnimation);
            addTimeButton.setVisibility(View.VISIBLE);
            addTriggerButton.setVisibility(View.VISIBLE);
        } else {
            RotateAnimation animation =
                    new RotateAnimation(45, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(300);
            animation.setRepeatMode(RotateAnimation.REVERSE);
            animation.setFillAfter(true);
            addButton.startAnimation(animation);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.2f);
            alphaAnimation.setDuration(150);
            addTimeButton.startAnimation(alphaAnimation);
            addTriggerButton.startAnimation(alphaAnimation);
            addTimeButton.setVisibility(View.GONE);
            addTriggerButton.setVisibility(View.GONE);
        }
    }

    private void generateTimeTaskList(ArrayList<Integer> integerArrayList, ArrayList<Map<String, Object>> tasksData) {
        //时间触发
        final SQLiteDatabase db = ScheduledTasksManageActivity.this.openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        final Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                try {
                    String label = cursor.getString(cursor.getColumnIndex("label"));
                    String hour = Integer.toString(cursor.getInt(cursor.getColumnIndex("hour")));
                    String minutes = Integer.toString(cursor.getInt(cursor.getColumnIndex("minutes")));
                    String time = (hour.length() == 1 ? "0" + hour : hour) + ":" + (minutes.length() == 1 ? "0" + minutes : minutes);
                    int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                    Map<String, Object> keyValuePair = new HashMap<>();
                    keyValuePair.put("label", label);
                    keyValuePair.put("time", time);
                    keyValuePair.put("isTimeTask", true);
                    keyValuePair.put("enabled", enabled == 1 ? themeDotResId : R.drawable.shapedotwhite);
                    tasksData.add(keyValuePair);
                    integerArrayList.add(cursor.getInt(cursor.getColumnIndex("_id")));
                    cursor.moveToNext();
                } catch (Exception e) {
                    //找不到报错原因，先catch了，等相关信息多了想办法定位
                    showToast(this, e.getLocalizedMessage());
                }
            }
        }
        cursor.close();
        db.close();
    }

    private void generateTriggerTaskList(ArrayList<Integer> integerArrayList, ArrayList<Map<String, Object>> tasksData) {
        //事件触发器触发
        final SQLiteDatabase db = ScheduledTasksManageActivity.this.openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        final Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                try {
                    String label = cursor.getString(cursor.getColumnIndex("label"));
                    String tg = cursor.getString(cursor.getColumnIndex("tg"));
                    int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                    Map<String, Object> keyValuePair = new HashMap<>();
                    keyValuePair.put("label", label);
                    keyValuePair.put("time",
                            Arrays.asList(getResources().getStringArray(R.array.triggers))
                                    .get(Arrays.asList(getResources().getStringArray(R.array.triggersValues)).indexOf(tg)));
                    keyValuePair.put("isTimeTask", false);
                    keyValuePair.put("enabled", enabled == 1 ? themeDotResId : R.drawable.shapedotwhite);
                    tasksData.add(keyValuePair);
                    integerArrayList.add(cursor.getInt(cursor.getColumnIndex("_id")));
                    cursor.moveToNext();
                } catch (Exception e) {
                    //找不到报错原因，先catch了，等相关信息多了想办法定位
                    showToast(this, e.getLocalizedMessage());
                }
            }
        }
        cursor.close();
        db.close();
    }

    private void updateTasksList() {
        final ListView tasksListView = findViewById(R.id.stma_tasksListview);
        ScheduledTasksManageSimpleAdapter adapter = (ScheduledTasksManageSimpleAdapter) tasksListView.getAdapter();
        if (adapter != null) {
            ArrayList<Map<String, Object>> tasksData = new ArrayList<>();
            integerArrayList.clear();
            generateTimeTaskList(integerArrayList, tasksData);
            generateTriggerTaskList(integerArrayList, tasksData);
            adapter.replaceAllInFormerArrayList(tasksData);
        }
    }
}
