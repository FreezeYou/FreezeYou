package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R

@Keep
class SettingsBackgroundServiceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_background_service, rootKey)
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.backgroundService)
    }

}