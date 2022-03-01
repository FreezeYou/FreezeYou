package cf.playhi.freezeyou.ui.fragment.settings

import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.ui.UriAutoAllowManageActivity

@Keep
class SettingsInstallUninstallFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_install_uninstall, rootKey)

        findPreference<Preference?>("manageIpaAutoAllow")?.intent = Intent(
            requireActivity(),
            UriAutoAllowManageActivity::class.java
        )
            .putExtra("isIpaMode", true)

        findPreference<CheckBoxPreference?>(tryDelApkAfterInstalled.name)?.run {
            preferenceDataStore = DefaultMultiProcessMMKVDataStore()
            isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                tryDelApkAfterInstalled.name,
                tryDelApkAfterInstalled.defaultValue()
            )
        }
        findPreference<CheckBoxPreference?>(notAllowInstallWhenIsObsd.name)?.run {
            preferenceDataStore = DefaultMultiProcessMMKVDataStore()
            isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                notAllowInstallWhenIsObsd.name,
                notAllowInstallWhenIsObsd.defaultValue()
            )
        }
        findPreference<CheckBoxPreference?>(tryToAvoidUpdateWhenUsing.name)?.run {
            preferenceDataStore = DefaultMultiProcessMMKVDataStore()
            isChecked = DefaultMultiProcessMMKVDataStore().getBoolean(
                tryToAvoidUpdateWhenUsing.name,
                tryToAvoidUpdateWhenUsing.defaultValue()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.installAndUninstall)
    }

}