package cf.playhi.freezeyou.ui.fragment.settings

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cf.playhi.freezeyou.DeviceAdminReceiver
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.utils.AlertDialogUtils.buildAlertDialog
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isProfileOwner
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import cf.playhi.freezeyou.utils.VersionUtils.checkUpdate

@Keep
class SettingsDangerZoneFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spr_danger_zone, rootKey)

        if (isDeviceOwner(activity) || isProfileOwner(activity)) {
            findPreference<Preference?>("clearAllUserData")?.let {
                preferenceScreen?.removePreference(it)
            }
        }

        findPreference<Preference?>("clearAllUserData")?.setOnPreferenceClickListener {
            buildAlertDialog(
                requireActivity(),
                R.drawable.ic_warning,
                R.string.clearAllUserData,
                R.string.notice
            )
                .setPositiveButton(R.string.yes) { _, _ ->
                    val activityManager =
                        requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
                    if (activityManager != null && Build.VERSION.SDK_INT >= 19) {
                        try {
                            showToast(
                                requireActivity(),
                                if (activityManager.clearApplicationUserData())
                                    R.string.success
                                else
                                    R.string.failed
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showToast(requireActivity(), R.string.failed)
                        }
                    } else {
                        showToast(requireActivity(), R.string.sysVerLow)
                    }
                }
                .setNegativeButton(R.string.no, null)
                .show()
            true
        }

        findPreference<Preference?>("selfRevokeProfileOwner")?.setOnPreferenceClickListener {
            buildAlertDialog(
                requireActivity(),
                R.drawable.ic_warning,
                R.string.selfRevokeProfileOwner,
                R.string.plsConfirm
            )
                .setPositiveButton(R.string.yes) { _, _ ->
                    if (isProfileOwner(requireContext())) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                @Suppress("DEPRECATION")
                                DevicePolicyManagerUtils
                                    .getDevicePolicyManager(
                                        requireContext()
                                    )
                                    .clearProfileOwner(
                                        ComponentName(
                                            requireContext(),
                                            DeviceAdminReceiver::class.java
                                        )
                                    )
                                showToast(requireActivity(), R.string.success)
                            } else {
                                // TODO: Unsupported
                            }
                        } catch (e: SecurityException) {
                            // TODO: Is not an active profile owner, or the method is being called from a managed profile.
                        }
                    } else {
                        // TODO: Is not an active profile owner
                    }
                }
                .setNegativeButton(R.string.no, null)
                .setNeutralButton(R.string.update) { _, _ ->
                    checkUpdate(requireActivity())
                }
                .show()
            true
        }

        findPreference<Preference?>("uninstall")?.setOnPreferenceClickListener {
            activity?.startActivity(
                Intent(
                    Intent.ACTION_DELETE,
                    Uri.parse("package:cf.playhi.freezeyou")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )

            true
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.dangerZone)
    }

}