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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.Support.getThemeDot;
import static cf.playhi.freezeyou.Support.processActionBar;
import static cf.playhi.freezeyou.Support.processSetTheme;

public class ScheduledTasksManageActivity extends Activity {
    int themeDotResId;

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
        final SQLiteDatabase db = ScheduledTasksManageActivity.this.openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        final Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            final List<Map<String, Object>> tasksData = new ArrayList<>();
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
                integerArrayList.add(cursor.getInt(cursor.getColumnIndex("hour")));
                cursor.moveToNext();
            }
            ListAdapter adapter =
                    new SimpleAdapter(this, tasksData, R.layout.stma_item, new String[]{"label",
                            "time", "enabled"}, new int[]{R.id.stma_label, R.id.stma_time, R.id.stma_status});
            tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    HashMap<String, Object> map = (HashMap<String, Object>) tasksListView.getItemAtPosition(i);
                    final String label = (String) map.get("label");
                    startActivityForResult(
                            new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                    .putExtra("label", label)
                                    .putExtra("id", integerArrayList.get(i)),
                            1);
                }
            });
            tasksListView.setAdapter(adapter);
        }
        cursor.close();
        db.close();
    }

    private void processAddButton() {
        ImageButton addButton = findViewById(R.id.stma_addButton);
        addButton.setBackgroundResource(Build.VERSION.SDK_INT >= 21 ? R.drawable.oval_ripple : themeDotResId);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(ScheduledTasksManageActivity.this, ScheduledTasksAddActivity.class)
                                .putExtra("label", getString(R.string.add)),
                        1);
            }
        });
    }
}
