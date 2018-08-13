package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import net.grandcentrix.tray.provider.SqliteHelper;

import java.util.Set;

import static cf.playhi.freezeyou.Support.processActionBar;
import static cf.playhi.freezeyou.Support.processSetTheme;

public class ScheduledTasksAddActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stma_add);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.staa_sp, new STAAFragment())
                .commit();
        ActionBar actionBar = getActionBar();
        processActionBar(actionBar);
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("label"));
        }
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

    private void init() {
        ImageButton saveButton = findViewById(R.id.staa_saveButton);
        if (Build.VERSION.SDK_INT < 21) {
            try {
                switch (PreferenceManager.getDefaultSharedPreferences(this).getString("uiStyleSelection", "default")) {
                    case "orange":
                        saveButton.setBackgroundResource(R.drawable.shapedotorange);
                        break;
                    case "blue":
                        saveButton.setBackgroundResource(R.drawable.shapedotblue);
                        break;
                    case "green":
                        saveButton.setBackgroundResource(R.drawable.shapedotgreen);
                        break;
                    case "yellow":
                        saveButton.setBackgroundResource(R.drawable.shapedotyellow);
                        break;
                    case "pink":
                        saveButton.setBackgroundResource(R.drawable.shapedotpink);
                        break;
                    default:
                        saveButton.setBackgroundResource(R.drawable.shapedotblack);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            saveButton.setBackgroundResource(R.drawable.oval_ripple);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String time = defaultSharedPreferences.getString("stma_add_time", "09:09");
                int hour = Integer.valueOf(time.substring(0, time.indexOf(":")));
                int minutes = Integer.valueOf(time.substring(time.indexOf(":") + 1));
                int enabled = defaultSharedPreferences.getBoolean("stma_add_enable", true) ? 1 : 0;
                StringBuilder repeatStringBuilder = new StringBuilder();
                Set<String> stringSet = defaultSharedPreferences.getStringSet("stma_add_repeat", null);
                if (stringSet != null) {
                    for (String str : stringSet) {
                        switch (str) {
                            case "1":
                                repeatStringBuilder.append(str);
                                break;
                            case "2":
                                repeatStringBuilder.append(str);
                                break;
                            case "3":
                                repeatStringBuilder.append(str);
                                break;
                            case "4":
                                repeatStringBuilder.append(str);
                                break;
                            case "5":
                                repeatStringBuilder.append(str);
                                break;
                            case "6":
                                repeatStringBuilder.append(str);
                                break;
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
                SQLiteDatabase db = ScheduledTasksAddActivity.this.openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
                db.execSQL(
                        "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
                );//column1\2 留作备用
                db.execSQL(
                        "insert into tasks(_id,hour,minutes,repeat,enabled,label,task,column1,column2) values(null,"
                                + hour + ","
                                + minutes + ","
                                + repeat + ","
                                + enabled + ","
                                + "'" + label + "'" + ","
                                + "'" + task + "'" + ",'','')"
                );
                db.close();
            }
        });
    }
}
