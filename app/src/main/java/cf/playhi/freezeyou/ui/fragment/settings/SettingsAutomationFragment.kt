package cf.playhi.freezeyou.ui.fragment.settings

import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.ui.UriAutoAllowManageActivity
import cf.playhi.freezeyou.utils.AlertDialogUtils.buildAlertDialog
import java.io.File

@Keep
class SettingsAutomationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DefaultMultiProcessMMKVDataStore()

        setPreferencesFromResource(R.xml.spr_automation, rootKey)

        findPreference<Preference?>("manageIpaAutoAllow")?.intent = Intent(
            requireActivity(),
            UriAutoAllowManageActivity::class.java
        )
            .putExtra("isIpaMode", true)

        findPreference<Preference?>("deleteAllScheduledTasks")?.setOnPreferenceClickListener {
            buildAlertDialog(
                requireActivity(),
                R.drawable.ic_warning,
                R.string.askIfDel,
                R.string.caution
            )
                .setPositiveButton(R.string.yes) { _, _ ->
                    var file: File
                    for (name in arrayOf(
                        "scheduledTasks",
                        "scheduledTriggerTasks"
                    )) {
                        file = requireContext().getDatabasePath(name)
                        if (file.exists()) file.delete()
                    }
                }
                .setNegativeButton(R.string.no, null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.automation)
    }

}