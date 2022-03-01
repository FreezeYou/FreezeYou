package cf.playhi.freezeyou.ui

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.languagePref
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.allowFollowSystemAutoSwitchDarkMode
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys.uiStyleSelection
import cf.playhi.freezeyou.ui.fragment.settings.SettingsFragment
import cf.playhi.freezeyou.utils.SettingsUtils.syncAndCheckSharedPreference
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import net.grandcentrix.tray.AppPreferences

class SettingsActivity : FreezeYouBaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
        processActionBar(supportActionBar)
    }

    override fun finish() {
        if (Build.VERSION.SDK_INT >= 21 && !AppPreferences(this).getBoolean(
                "showInRecents",
                true
            )
        ) {
            finishAndRemoveTask()
        }
        super.finish()
    }

    override fun onPreferenceStartScreen(
        preferenceFragmentCompat: PreferenceFragmentCompat, preferenceScreen: PreferenceScreen
    ): Boolean {
        val ft = supportFragmentManager.beginTransaction()
        val fragment = SettingsFragment()
        val args = Bundle()
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.key)
        fragment.arguments = args
        ft.replace(R.id.content, fragment, preferenceScreen.key)
        ft.addToBackStack(preferenceScreen.key)
        ft.commit()
        return true
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val prefFragment = pref.fragment
        if (prefFragment != null) {
            val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                prefFragment
            )
            fragment.arguments = args
            fragment.setTargetFragment(caller, 0)
            // Replace the existing Fragment with the new Fragment
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit()
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val fragmentManager = supportFragmentManager
            if (fragmentManager.backStackEntryCount == 0) {
                finish()
            } else {
                fragmentManager.popBackStack()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        syncAndCheckSharedPreference(applicationContext, this, sharedPreferences, s)
        if (languagePref.name == s
            || uiStyleSelection.name == s
            || allowFollowSystemAutoSwitchDarkMode.name == s
        ) {
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}