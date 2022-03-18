package cf.playhi.freezeyou.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_LEGACY_AUTO
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_MROOT_DPM
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_MROOT_PROFILE_OWNER
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_ROOT_DISABLE_ENABLE
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_ROOT_UNHIDE_HIDE
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER
import cf.playhi.freezeyou.service.ScreenLockOneKeyFreezeService
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.*
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys.mainActivityPattern
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys.organizationName
import cf.playhi.freezeyou.utils.ToastUtils.showToast

object SettingsUtils {

    /**
     * Show toast, check availability, sync to MMKV, etc.
     */
    @JvmStatic
    fun checkPreferenceData(
        context: Context, activity: Activity,
        sharedPreferences: SharedPreferences?, key: String?
    ) {
        if (key == null || sharedPreferences == null) return

        syncMMKVDataToMMKVWhenSharedPreferenceDataChanged(context, sharedPreferences, key)

        when (key) {
            uiStyleSelection.name,
            allowFollowSystemAutoSwitchDarkMode.name,
            mainActivityPattern.name,
            languagePref.name ->
                showToast(
                    activity,
                    R.string.willTakeEffectsNextLaunch
                )
            onekeyFreezeWhenLockScreen.name -> {
                if (DefaultMultiProcessMMKVDataStore()
                        .getBoolean(key, onekeyFreezeWhenLockScreen.defaultValue())
                ) {
                    ServiceUtils.startService(
                        context,
                        Intent(context, ScreenLockOneKeyFreezeService::class.java)
                    )
                } else {
                    context.stopService(Intent(context, ScreenLockOneKeyFreezeService::class.java))
                }
            }
            freezeOnceQuit.name,
            avoidFreezeForegroundApplications.name,
            tryToAvoidUpdateWhenUsing.name -> {
                if (DefaultMultiProcessMMKVDataStore().getBoolean(
                        key,
                        tryToAvoidUpdateWhenUsing.defaultValue()
                    ) && !AccessibilityUtils.isAccessibilitySettingsOn(context)
                ) {
                    showToast(activity, R.string.needActiveAccessibilityService)
                    AccessibilityUtils.openAccessibilitySettings(context)
                }
            }
            organizationName.name ->
                DevicePolicyManagerUtils.checkAndSetOrganizationName(
                    context,
                    sharedPreferences.getString(key, null)
                )
            avoidFreezeNotifyingApplications.name -> {
                if (Build.VERSION.SDK_INT >= 21) {
                    val enabledNotificationListeners = Settings.Secure.getString(
                        context.contentResolver, "enabled_notification_listeners"
                    )
                    if (enabledNotificationListeners != null
                        && !enabledNotificationListeners.contains("cf." + "playhi." + "freezeyou")
                    ) {
                        try {
                            activity.startActivity(
                                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                            )
                        } catch (e: Exception) {
                            showToast(activity, R.string.failed)
                        }
                    }
                }
            }
            enableInstallPkgFunc.name ->
                if (sharedPreferences.getBoolean(key, enableInstallPkgFunc.defaultValue())) {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.InstallPackagesActivity"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } else {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.InstallPackagesActivity"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            selectFUFMode.name -> {
                when (DefaultMultiProcessMMKVDataStore()
                    .getString(selectFUFMode.name, selectFUFMode.defaultValue())?.toInt()) {
                    API_FREEZEYOU_MROOT_DPM ->
                        if (!DevicePolicyManagerUtils.isDeviceOwner(context)) {
                            showToast(context, R.string.noMRootPermission)
                        }
                    API_FREEZEYOU_MROOT_PROFILE_OWNER ->
                        if (!DevicePolicyManagerUtils.isProfileOwner(context)) {
                            showToast(context, R.string.isNotProfileOwner)
                        }
                    API_FREEZEYOU_ROOT_DISABLE_ENABLE,
                    API_FREEZEYOU_ROOT_UNHIDE_HIDE ->
                        if (!FUFUtils.checkRootPermission()) {
                            showToast(context, R.string.noRootPermission)
                        }
                    @Suppress("DEPRECATION")
                    API_FREEZEYOU_LEGACY_AUTO ->
                        if (!(FUFUtils.checkRootPermission()
                                    || DevicePolicyManagerUtils.isDeviceOwner(context))
                        ) {
                            showToast(context, R.string.insufficientPermission)
                        }
                    API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED,
                    API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER,
                    API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE ->
                        if (!FUFUtils.isSystemApp(context)) {
                            showToast(context, R.string.insufficientPermission)
                        }
                    else -> showToast(context, R.string.unknown)
                }
            }
        }
    }

    private fun syncMMKVDataToMMKVWhenSharedPreferenceDataChanged(
        context: Context,
        sharedPreferences: SharedPreferences,
        key: String
    ) {
        try {
            DefaultMultiProcessMMKVStorageBooleanKeys.valueOf(key).run {
                setValue(context, sharedPreferences.getBoolean(key, defaultValue()))
            }
            return
        } catch (_: IllegalArgumentException) {
        }

        try {
            DefaultMultiProcessMMKVStorageStringKeys.valueOf(key).run {
                setValue(context, sharedPreferences.getString(key, defaultValue()))
            }
            return
        } catch (_: IllegalArgumentException) {
        }
    }

    fun changeIconEntryComponentState(context: Context, newValue: Boolean, cls: String) {
        if (newValue) {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, cls),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, cls),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        showToast(context, R.string.ciFinishedToast)
    }
}