package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.ImageButton;

import static cf.playhi.freezeyou.Support.processActionBar;
import static cf.playhi.freezeyou.Support.processSetTheme;

public class TimedTasksManageActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ttma_main);
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

    private void init() {
        ImageButton addButton = findViewById(R.id.ttma_addButton);
        try {
            switch (PreferenceManager.getDefaultSharedPreferences(this).getString("uiStyleSelection", "default")) {
                case "blue":
                    addButton.setBackgroundResource(R.drawable.shapedotblue);
                    break;
                case "orange":
                    addButton.setBackgroundResource(R.drawable.shapedotorange);
                    break;
                case "black":
                    addButton.setBackgroundResource(R.drawable.shapedotblack);
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
                    if (Build.VERSION.SDK_INT >= 21) {
                        addButton.setBackgroundResource(R.drawable.shapedotblue);
                    } else {
                        addButton.setBackgroundResource(R.drawable.shapedotblack);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
