package cf.playhi.freezeyou.ui;

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
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.adapter.ScheduledTasksManageSimpleAdapter;
import cf.playhi.freezeyou.utils.ThemeUtils;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.AlertDialogUtils;
import cf.playhi.freezeyou.utils.TasksUtils;

import static cf.playhi.freezeyou.utils.ThemeUtils.getThemeFabDotBackground;
import static cf.playhi.freezeyou.utils.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;

public class ScheduledTasksManageActivity extends FreezeYouBaseActivity {

    private final ArrayList<Integer> integerArrayList = new ArrayList<>();
    private final ArrayList<Integer> selectedTasksPositions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stma_main);
        processActionBar(getSupportActionBar());
        init();
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
        final ListView tasksListView = findViewById(R.id.stma_tasksListview);
        final ArrayList<Map<String, Object>> tasksData = new ArrayList<>();

        generateTimeTaskList(integerArrayList, tasksData);
        generateTriggerTaskList(integerArrayList, tasksData);

        ScheduledTasksManageSimpleAdapter adapter =
                new ScheduledTasksManageSimpleAdapter(this, tasksData, integerArrayList,
                        R.layout.stma_item, new String[]{"label", "time", "enabled"},
                        new int[]{R.id.stma_label, R.id.stma_time, R.id.stma_switch});

        tasksListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    if (!selectedTasksPositions.contains(position)) {
                        selectedTasksPositions.add(position);
                    }
                } else {
                    selectedTasksPositions.remove(Integer.valueOf(position));
                }
                mode.setTitle(Integer.toString(selectedTasksPositions.size()));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                ScheduledTasksManageActivity.this.getMenuInflater().inflate(R.menu.stma_multichoicemenu, menu);
                String cTheme = ThemeUtils.getUiTheme(ScheduledTasksManageActivity.this);
                if ("white".equals(cTheme) || "default".equals(cTheme))
                    menu.findItem(R.id.stma_menu_mc_delete).setIcon(R.drawable.ic_action_delete_light);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.stma_menu_mc_delete:
                        AlertDialogUtils
                                .buildAlertDialog(
                                        ScheduledTasksManageActivity.this,
                                        R.drawable.ic_warning, R.string.askIfDel, R.string.notice
                                )
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    for (int aSelectedTaskPosition : selectedTasksPositions) {
                                        boolean isTimeTask =
                                                (boolean) (((ScheduledTasksManageSimpleAdapter) tasksListView.getAdapter())
                                                        .getStoredArrayList().get(aSelectedTaskPosition)).get("isTimeTask");
                                        int id = integerArrayList.get(aSelectedTaskPosition);
                                        SQLiteDatabase db = openOrCreateDatabase(isTimeTask ? "scheduledTasks" : "scheduledTriggerTasks", MODE_PRIVATE, null);
                                        if (isTimeTask) {
                                            TasksUtils.cancelTheTask(ScheduledTasksManageActivity.this, id);
                                        }
                                        db.execSQL("DELETE FROM tasks WHERE _id = " + id);
                                        db.close();
                                    }
                                    mode.finish();
                                })
                                .setNegativeButton(R.string.no, null)
                                .create().show();
                        return true;
                    case R.id.stma_menu_mc_selectAll:
                        for (int i = 0; i < tasksListView.getAdapter().getCount(); i++) {
                            tasksListView.setItemChecked(i, true);
                        }
                        return true;
                    case R.id.stma_menu_mc_selectUnselected:
                        for (int i = 0; i < tasksListView.getAdapter().getCount(); i++) {
                            tasksListView.setItemChecked(i, !tasksListView.isItemChecked(i));
                        }
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

        tasksListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Map<String, Object> map =
                    ((ScheduledTasksManageSimpleAdapter) tasksListView.getAdapter())
                            .getStoredArrayList().get(i);
            final String label = (String) map.get("label");
            final String s = ((String) map.get("time"));
            final boolean isTimeTask = ((s != null) && s.contains(":"));
            startActivityForResult(
                    new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                            .putExtra("label", label)
                            .putExtra("time", isTimeTask)
                            .putExtra("id", integerArrayList.get(i)),
                    isTimeTask ? 1 : 2);
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
            addButton.setBackgroundResource(getThemeFabDotBackground(this));
        }

        addButton.setOnClickListener(view ->
                changeFloatButtonsStatus(addTimeButton.getVisibility() != View.VISIBLE)
        );

        addTimeButton.setOnClickListener(v -> {
            changeFloatButtonsStatus(false);
            startActivityForResult(
                    new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                            .putExtra("label", getString(R.string.add))
                            .putExtra("time", true),
                    1);
        });

        addTriggerButton.setOnClickListener(v -> {
            changeFloatButtonsStatus(false);
            startActivityForResult(
                    new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                            .putExtra("label", getString(R.string.add))
                            .putExtra("time", false),
                    2);
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
                String label = cursor.getString(cursor.getColumnIndex("label"));
                String hour = Integer.toString(cursor.getInt(cursor.getColumnIndex("hour")));
                String minutes = Integer.toString(cursor.getInt(cursor.getColumnIndex("minutes")));
                String time = (hour.length() == 1 ? "0" + hour : hour) + ":" + (minutes.length() == 1 ? "0" + minutes : minutes);
                int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                Map<String, Object> keyValuePair = new HashMap<>();
                keyValuePair.put("label", label);
                keyValuePair.put("time", time);
                keyValuePair.put("isTimeTask", true);
                keyValuePair.put("enabled", enabled == 1);
                tasksData.add(keyValuePair);
                integerArrayList.add(cursor.getInt(cursor.getColumnIndex("_id")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
    }

    private void generateTriggerTaskList(ArrayList<Integer> integerArrayList, ArrayList<Map<String, Object>> tasksData) {
        //事件触发器触发
        final SQLiteDatabase db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        final Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String tg = cursor.getString(cursor.getColumnIndex("tg"));
                int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                String label = cursor.getString(cursor.getColumnIndex("label"));
                Map<String, Object> keyValuePair = new HashMap<>();
                keyValuePair.put("label", label);
                int indexOf = Arrays.asList(getResources().getStringArray(R.array.triggersValues)).indexOf(tg);
                keyValuePair.put("time",
                        Arrays.asList(getResources().getStringArray(R.array.triggers))
                                .get(indexOf == -1 ? 0 : indexOf));
                keyValuePair.put("isTimeTask", false);
                keyValuePair.put("enabled", enabled == 1);
                tasksData.add(keyValuePair);
                integerArrayList.add(cursor.getInt(cursor.getColumnIndex("_id")));
                cursor.moveToNext();
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
