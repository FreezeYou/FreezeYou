package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R

@Keep
class SettingsCommonFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_common, rootKey)
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.common)
    }
}