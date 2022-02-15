package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.utils.NotificationUtils.startAppNotificationSettingsSystemActivity

@Keep
class SettingsNotificationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_notification, rootKey)

        findPreference<Preference?>("notificationBar_more")?.setOnPreferenceClickListener {
            startAppNotificationSettingsSystemActivity(
                requireActivity(), requireContext().packageName,
                requireContext().applicationInfo.uid
            )
            true
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.notificationBar)
    }
}