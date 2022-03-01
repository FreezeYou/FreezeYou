package cf.playhi.freezeyou.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import cf.playhi.freezeyou.DeviceAdminReceiver
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
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.*
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys.*
import cf.playhi.freezeyou.utils.ToastUtils.showToast

object SettingsUtils {
    /**
     * Reserved for importing backups in v1 format.
     *
     * @param context           Context
     * @param activity          Activity
     * @param sharedPreferences SharedPreference
     * @param s                 Preference key
     */
    @JvmStatic
    fun syncAndCheckSharedPreference(
        context: Context, activity: Activity,
        sharedPreferences: SharedPreferences, s: String
    ) {
        // TODO: DefaultMultiProcessMMKVStorageBooleanKeys.valueOf(s) (把 Keys 都合起来？再想想～)
        when (s) {
            firstIconEnabled.name -> {
                if (sharedPreferences.getBoolean(s, firstIconEnabled.defaultValue())) {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.FirstIcon"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } else {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.FirstIcon"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                showToast(activity, R.string.ciFinishedToast)
            }
            secondIconEnabled.name -> {
                if (sharedPreferences.getBoolean(s, secondIconEnabled.defaultValue())) {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.SecondIcon"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } else {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.SecondIcon"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                showToast(activity, R.string.ciFinishedToast)
            }
            thirdIconEnabled.name -> {
                if (sharedPreferences.getBoolean(s, thirdIconEnabled.defaultValue())) {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.ThirdIcon"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } else {
                    context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, "cf.playhi.freezeyou.ThirdIcon"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                showToast(activity, R.string.ciFinishedToast)
            }
            shortCutOneKeyFreezeAdditionalOptions.name -> {
                DefaultMultiProcessMMKVDataStore()
                    .putString(
                        s,
                        sharedPreferences.getString(
                            s,
                            shortCutOneKeyFreezeAdditionalOptions.defaultValue()
                        )
                    )
                if (shortCutOneKeyFreezeAdditionalOptions.defaultValue()
                    != sharedPreferences.getString(
                        s,
                        shortCutOneKeyFreezeAdditionalOptions.defaultValue()
                    )
                ) {
                    val devicePolicyManager =
                        DevicePolicyManagerUtils.getDevicePolicyManager(context)
                    if (devicePolicyManager != null && !devicePolicyManager.isAdminActive(
                            ComponentName(context, DeviceAdminReceiver::class.java)
                        )
                    ) {
                        DevicePolicyManagerUtils.openDevicePolicyManager(activity)
                    }
                }
            }
            uiStyleSelection.name,
            allowFollowSystemAutoSwitchDarkMode.name,
            languagePref.name,
            mainActivityPattern.name ->
                showToast(
                    activity,
                    R.string.willTakeEffectsNextLaunch
                )
            languagePref.name -> {
                DefaultMultiProcessMMKVDataStore().putString(
                    s,
                    sharedPreferences.getString(
                        s,
                        languagePref.defaultValue()
                    )
                )
                showToast(
                    activity,
                    R.string.willTakeEffectsNextLaunch
                )
            }
            onekeyFreezeWhenLockScreen.name -> {
                DefaultMultiProcessMMKVDataStore()
                    .putBoolean(
                        s,
                        sharedPreferences.getBoolean(
                            s,
                            onekeyFreezeWhenLockScreen.defaultValue()
                        )
                    )
                if (sharedPreferences.getBoolean(s, onekeyFreezeWhenLockScreen.defaultValue())) {
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
                DefaultMultiProcessMMKVDataStore()
                    .putBoolean(s, sharedPreferences.getBoolean(s, false))
                if (sharedPreferences.getBoolean(
                        s,
                        false
                    ) && !AccessibilityUtils.isAccessibilitySettingsOn(context)
                ) {
                    showToast(activity, R.string.needActiveAccessibilityService)
                    AccessibilityUtils.openAccessibilitySettings(context)
                }
            }
            useForegroundService.name,
            openImmediately.name,
            openAndUFImmediately.name,
            notificationBarDisableSlideOut.name,
            notificationBarDisableClickDisappear.name,
            lesserToast.name,
            debugModeEnabled.name,
            tryDelApkAfterInstalled.name ->
                DefaultMultiProcessMMKVDataStore()
                    .putBoolean(s, sharedPreferences.getBoolean(s, false))
            notificationBarFreezeImmediately.name,
            showInRecents.name,
            notAllowInstallWhenIsObsd.name,
            createQuickFUFNotiAfterUnfrozen.name ->
                DefaultMultiProcessMMKVDataStore().putBoolean(
                    s,
                    sharedPreferences.getBoolean(s, true)
                )
            organizationName.name ->
                DevicePolicyManagerUtils.checkAndSetOrganizationName(
                    context,
                    sharedPreferences.getString(s, null)
                )
            avoidFreezeNotifyingApplications.name -> {
                DefaultMultiProcessMMKVDataStore()
                    .putBoolean(
                        s, sharedPreferences.getBoolean(
                            s, avoidFreezeNotifyingApplications.defaultValue()
                        )
                    )
                if (Build.VERSION.SDK_INT >= 21) { // 这项不兼容 5.0 以下了
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
                if (sharedPreferences.getBoolean(s, enableInstallPkgFunc.defaultValue())) {
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
                DefaultMultiProcessMMKVDataStore()
                    .putString(s, sharedPreferences.getString(s, selectFUFMode.defaultValue()))
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
            else -> {}
        }
    }
}