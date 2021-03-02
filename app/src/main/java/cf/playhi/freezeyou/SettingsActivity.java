package cf.playhi.freezeyou;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import net.grandcentrix.tray.AppPreferences;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class SettingsActivity extends FreezeYouBaseActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
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

    @Override
    public boolean onPreferenceStartScreen(
            PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);
        ft.replace(android.R.id.content, fragment, preferenceScreen.getKey());
        ft.addToBackStack(preferenceScreen.getKey());
        ft.commit();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() == 0) {
                finish();
            } else {
                fragmentManager.popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
