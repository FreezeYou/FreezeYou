package cf.playhi.freezeyou.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import static cf.playhi.freezeyou.utils.Support.checkLanguage;

public class FreezeYouBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLanguage(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
