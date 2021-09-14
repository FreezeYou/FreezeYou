package cf.playhi.freezeyou

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.PreferenceFragmentCompat

@Keep
class SettingsNotificationFufFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_notification_fuf, rootKey)
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.freezeAUF)
    }
}