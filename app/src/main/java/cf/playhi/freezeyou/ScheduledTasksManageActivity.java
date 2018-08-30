package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.Support.getThemeDot;
import static cf.playhi.freezeyou.Support.processActionBar;
import static cf.playhi.freezeyou.Support.processSetTheme;

public class ScheduledTasksManageActivity extends Activity {
    private int themeDotResId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
                if (resultCode == RESULT_OK)
                    generateTasksList();
                break;
            case 2:
                if (resultCode == RESULT_OK)
                    generateTasksList();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init() {
        generateTasksList();
        processAddButton();
    }

    private void generateTasksList() {
        final ArrayList<Integer> integerArrayList = new ArrayList<>();
        themeDotResId = getThemeDot(ScheduledTasksManageActivity.this);
        final ListView tasksListView = findViewById(R.id.stma_tasksListview);
        final List<Map<String, Object>> tasksData = new ArrayList<>();

        generateTimeTaskList(integerArrayList, tasksData);
        generateTriggerTaskList(integerArrayList, tasksData);

        ListAdapter adapter =
                new SimpleAdapter(this, tasksData, R.layout.stma_item, new String[]{"label",
                        "time", "enabled"}, new int[]{R.id.stma_label, R.id.stma_time, R.id.stma_status});
        tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, Object> map = (HashMap<String, Object>) tasksListView.getItemAtPosition(i);
                final String label = (String) map.get("label");
                String s = ((String) map.get("time"));
                boolean isTimeTask = ((s != null) && s.contains(":"));
                startActivityForResult(
                        new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                .putExtra("label", label)
                                .putExtra("time", isTimeTask)
                                .putExtra("id", integerArrayList.get(i)),
                        1);
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
                changeFloatButtonsStatus(!(addTimeButton.getVisibility() == View.VISIBLE));
            }
        });

        addTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFloatButtonsStatus(false);
                startActivityForResult(
                        new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                .putExtra("label", getString(R.string.add))
                                .putExtra("time", true),
                        1);
            }
        });

        addTriggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFloatButtonsStatus(false);
                startActivityForResult(
                        new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                .putExtra("label", getString(R.string.add))
                                .putExtra("time", false),
                        2);
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

    private void generateTimeTaskList(ArrayList<Integer> integerArrayList, List<Map<String, Object>> tasksData) {
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
                keyValuePair.put("enabled", enabled == 1 ? themeDotResId : R.drawable.shapedotwhite);
                tasksData.add(keyValuePair);
                integerArrayList.add(cursor.getInt(cursor.getColumnIndex("_id")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
    }

    private void generateTriggerTaskList(ArrayList<Integer> integerArrayList, List<Map<String, Object>> tasksData) {
        //事件触发器触发
        final SQLiteDatabase db = ScheduledTasksManageActivity.this.openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        final Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String label = cursor.getString(cursor.getColumnIndex("label"));
                String tg = cursor.getString(cursor.getColumnIndex("tg"));
                int enabled = cursor.getInt(cursor.getColumnIndex("enabled"));
                Map<String, Object> keyValuePair = new HashMap<>();
                keyValuePair.put("label", label);
                keyValuePair.put("time",
                        Arrays.asList(getResources().getStringArray(R.array.triggers))
                                .get(Arrays.asList(getResources().getStringArray(R.array.triggersValues)).indexOf(tg)));
                keyValuePair.put("enabled", enabled == 1 ? themeDotResId : R.drawable.shapedotwhite);
                tasksData.add(keyValuePair);
                integerArrayList.add(cursor.getInt(cursor.getColumnIndex("_id")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
    }
}
