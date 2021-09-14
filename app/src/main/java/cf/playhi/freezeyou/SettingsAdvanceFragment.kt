package cf.playhi.freezeyou

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.utils.AccessibilityUtils.openAccessibilitySettings

@Keep
class SettingsAdvanceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_advance, rootKey)

        findPreference<Preference?>("configureAccessibilityService")?.setOnPreferenceClickListener {
            openAccessibilitySettings(activity);
            true
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.advance)
    }

}