package cf.playhi.freezeyou.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R

@Keep
class SettingsFufFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_fuf, rootKey)
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.freezeAUF)
    }

}