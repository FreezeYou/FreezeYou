package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.Support.processActionBar;
import static cf.playhi.freezeyou.Support.processSetTheme;

public class ScheduledTasksManageActivity extends Activity {

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
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init() {
        ListView tasksListView = findViewById(R.id.stma_tasksListview);
        SQLiteDatabase db = ScheduledTasksManageActivity.this.openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        Cursor cursor = db.query("tasks", null, null, null, null, null, null);
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
                keyValuePair.put("enabled", enabled == 1);
                tasksData.add(keyValuePair);
                cursor.moveToNext();
            }
            ListAdapter adapter = new SimpleAdapter(this, tasksData,
                    R.layout.stma_item, new String[]{"label",
                    "time", "enabled"}, new int[]{R.id.stma_label, R.id.stma_time, R.id.stma_switchButton});
            tasksListView.setAdapter(adapter);
        }
        cursor.close();
        ImageButton addButton = findViewById(R.id.stma_addButton);
        if (Build.VERSION.SDK_INT >= 21) {
            addButton.setBackgroundResource(R.drawable.oval_ripple);
        } else {
            try {
                switch (PreferenceManager.getDefaultSharedPreferences(this).getString("uiStyleSelection", "default")) {
                    case "blue":
                        addButton.setBackgroundResource(R.drawable.shapedotblue);
                        break;
                    case "orange":
                        addButton.setBackgroundResource(R.drawable.shapedotorange);
                        break;
                    case "green":
                        addButton.setBackgroundResource(R.drawable.shapedotgreen);
                        break;
                    case "pink":
                        addButton.setBackgroundResource(R.drawable.shapedotpink);
                        break;
                    case "yellow":
                        addButton.setBackgroundResource(R.drawable.shapedotyellow);
                        break;
                    default:
                        addButton.setBackgroundResource(R.drawable.shapedotblack);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
