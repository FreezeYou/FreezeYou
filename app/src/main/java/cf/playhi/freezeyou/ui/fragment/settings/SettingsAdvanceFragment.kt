package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.debugModeEnabled
import cf.playhi.freezeyou.utils.AccessibilityUtils.openAccessibilitySettings

@Keep
class SettingsAdvanceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_advance, rootKey)

        findPreference<Preference?>("configureAccessibilityService")?.setOnPreferenceClickListener {
            openAccessibilitySettings(activity)
            true
        }

    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.advance)
    }

}