package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.lesserToast

@Keep
class SettingsCommonFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_common, rootKey)
        findPreference<CheckBoxPreference?>(lesserToast.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                    lesserToast.name, lesserToast.defaultValue()
                )
            }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.common)
    }
}