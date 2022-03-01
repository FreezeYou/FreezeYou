package cf.playhi.freezeyou.ui.fragment.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.enableAuthentication
import cf.playhi.freezeyou.ui.AppLockActivity
import cf.playhi.freezeyou.utils.AuthenticationUtils.isBiometricPromptPartAvailable

@Keep
class SettingsSecurityFragment : PreferenceFragmentCompat() {

    private var enableAuthenticationActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity: Activity? = activity
        if (activity != null) {
            enableAuthenticationActivityResultLauncher = registerForActivityResult(
                StartActivityForResult()
            ) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    (preferenceManager.preferenceDataStore as DefaultMultiProcessMMKVDataStore)
                        .putBoolean(enableAuthentication.name, true)

                    findPreference<CheckBoxPreference>(enableAuthentication.name)?.isChecked = true
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DefaultMultiProcessMMKVDataStore()

        setPreferencesFromResource(R.xml.spr_security, rootKey)


        val enableAuthenticationPreference: Preference? = findPreference(enableAuthentication.name)
        enableAuthenticationPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val activity: Activity? = activity
                if (activity != null) {
                    if (true == newValue) {
                        if (isBiometricPromptPartAvailable(activity)) {
                            if (enableAuthenticationActivityResultLauncher != null) {
                                enableAuthenticationActivityResultLauncher
                                    ?.launch(
                                        Intent(activity, AppLockActivity::class.java)
                                            .putExtra(
                                                "ignoreCurrentUnlockStatus",
                                                true
                                            )
                                    )
                            }
                        }
                        return@OnPreferenceChangeListener false
                    } else {
                        return@OnPreferenceChangeListener true
                    }
                }
                true
            }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.security)
    }


}
