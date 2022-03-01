package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.selectFUFMode
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.shortCutOneKeyFreezeAdditionalOptions

@Keep
class SettingsFufFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_fuf, rootKey)

        findPreference<ListPreference?>(selectFUFMode.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                value = DefaultMultiProcessMMKVDataStore().getString(
                    selectFUFMode.name, selectFUFMode.defaultValue()
                )
            }
        findPreference<ListPreference?>(shortCutOneKeyFreezeAdditionalOptions.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                value = DefaultMultiProcessMMKVDataStore().getString(
                    shortCutOneKeyFreezeAdditionalOptions.name,
                    shortCutOneKeyFreezeAdditionalOptions.defaultValue()
                )
            }
        findPreference<CheckBoxPreference?>(avoidFreezeForegroundApplications.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                    avoidFreezeForegroundApplications.name,
                    avoidFreezeForegroundApplications.defaultValue()
                )
            }
        findPreference<CheckBoxPreference?>(avoidFreezeNotifyingApplications.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                    avoidFreezeNotifyingApplications.name,
                    avoidFreezeNotifyingApplications.defaultValue()
                )
            }
        findPreference<CheckBoxPreference?>(openImmediately.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                    openImmediately.name,
                    openImmediately.defaultValue()
                )
            }
        findPreference<CheckBoxPreference?>(openAndUFImmediately.name)
            ?.run {
                preferenceDataStore = DefaultMultiProcessMMKVDataStore()
                isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                    openAndUFImmediately.name,
                    openAndUFImmediately.defaultValue()
                )
            }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.freezeAUF)
    }

}