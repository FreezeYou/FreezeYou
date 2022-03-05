package cf.playhi.freezeyou.ui.fragment.settings

import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
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

    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.installAndUninstall)
    }

}