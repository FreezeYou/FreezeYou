package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import static cf.playhi.freezeyou.Support.processActionBar;
import static cf.playhi.freezeyou.Support.processSetTheme;

public class ScheduledTasksAddActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ttma_add);
        ActionBar actionBar = getActionBar();
        processActionBar(actionBar);
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
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

    private void init(){

    }
}
