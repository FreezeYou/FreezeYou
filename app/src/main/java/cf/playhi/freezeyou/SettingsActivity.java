package cf.playhi.freezeyou;

import android.os.Build;
import android.os.Bundle;

import net.grandcentrix.tray.AppPreferences;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class SettingsActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        processActionBar(getSupportActionBar());
    }

    @Override
    public void finish() {
        if (Build.VERSION.SDK_INT >= 21 && !(new AppPreferences(this).getBoolean("showInRecents", true))) {
            finishAndRemoveTask();
        }
        super.finish();
    }
}
