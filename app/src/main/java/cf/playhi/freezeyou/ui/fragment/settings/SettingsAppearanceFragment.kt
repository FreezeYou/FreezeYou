package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.languagePref

@Keep
class SettingsAppearanceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_appearance, rootKey)

        findPreference<ListPreference?>(languagePref.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                value = DefaultMultiProcessMMKVDataStore().getString(
                    languagePref.name, languagePref.defaultValue()
                )
            }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.appearance)
    }
}