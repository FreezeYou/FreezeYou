package cf.playhi.freezeyou.ui.fragment.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.ui.BackupMainActivity
import cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite
import cf.playhi.freezeyou.utils.VersionUtils.checkUpdate

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.moreSettings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.spr, rootKey)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            preferenceScreen?.removePreferenceRecursively("backgroundService")
        }

    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val activity: Activity? = activity
        if (activity != null) {
            val key = preference.key
            if (key != null) {
                when (key) {
                    "checkUpdate" -> checkUpdate(activity)
                    "helpTranslate" -> requestOpenWebSite(
                        activity,
                        "https://github.com/FreezeYou/FreezeYou/blob/master/README_Translation.md"
                    )
                    "thanksList" -> requestOpenWebSite(
                        activity, String.format(
                            "https://www.zidon.net/%1\$s/thanks/",
                            getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                        )
                    )
                    "faq" -> requestOpenWebSite(
                        activity, String.format(
                            "https://www.zidon.net/%1\$s/faq/",
                            getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                        )
                    )
                    "backupAndRestore" -> startActivity(
                        Intent(
                            activity,
                            BackupMainActivity::class.java
                        )
                    )
                    "howToUse" -> requestOpenWebSite(
                        activity, String.format(
                            "https://www.zidon.net/%1\$s/guide/how-to-use.html",
                            getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                        )
                    )
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}