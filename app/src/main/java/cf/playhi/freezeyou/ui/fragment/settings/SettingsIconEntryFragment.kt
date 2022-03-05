package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.*
import cf.playhi.freezeyou.utils.SettingsUtils.changeIconEntryComponentState

@Keep
class SettingsIconEntryFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_icon_entry, rootKey)

        findPreference<CheckBoxPreference>(firstIconEnabled.name)?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Boolean)
                changeIconEntryComponentState(
                    requireContext(),
                    newValue,
                    "cf.playhi.freezeyou.FirstIcon"
                )
            true
        }

        findPreference<CheckBoxPreference>(secondIconEnabled.name)?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Boolean)
                changeIconEntryComponentState(
                    requireContext(),
                    newValue,
                    "cf.playhi.freezeyou.SecondIcon"
                )
            true
        }

        findPreference<CheckBoxPreference>(thirdIconEnabled.name)?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Boolean)
                changeIconEntryComponentState(
                    requireContext(),
                    newValue,
                    "cf.playhi.freezeyou.ThirdIcon"
                )
            true
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.iconAEntry)
    }

}