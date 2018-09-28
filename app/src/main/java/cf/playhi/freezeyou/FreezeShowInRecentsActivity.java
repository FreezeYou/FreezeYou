package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.Support.getApplicationIcon;
import static cf.playhi.freezeyou.Support.getApplicationLabel;
import static cf.playhi.freezeyou.Support.getBitmapFromDrawable;
import static cf.playhi.freezeyou.Support.makeDialog;
import static cf.playhi.freezeyou.Support.processAddTranslucent;
import static cf.playhi.freezeyou.Support.processSetTheme;

public class FreezeShowInRecentsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        processAddTranslucent(this);
        super.onCreate(savedInstanceState);

        String pkgName = getIntent().getStringExtra("pkgName");
        makeDialog(
                getIntent().getStringExtra("title"),
                getIntent().getStringExtra("message"),
                this,
                null,
                pkgName,
                getIntent().getBooleanExtra("ot", false),
                this,
                true
        );

        if (Build.VERSION.SDK_INT >= 21) {
            setTaskDescription(new ActivityManager.TaskDescription(getApplicationLabel(this, null, null, pkgName) + " - " + getString(R.string.app_name), getBitmapFromDrawable(getApplicationIcon(this, pkgName, null, false))));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
