package cf.playhi.freezeyou.fuf

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import cf.playhi.freezeyou.DeviceAdminReceiver.getComponentName
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.*
import cf.playhi.freezeyou.utils.FUFUtils.checkMRootFrozen
import cf.playhi.freezeyou.utils.FUFUtils.isSystemApp
import cf.playhi.freezeyou.utils.ProcessUtils.fAURoot

open class FUFSinglePackage(
    open val context: Context,
    open val singlePackageName: String,
    open val actionMode: Int,
    open val apiMode: Int
) {

    open suspend fun commit(): Int {
        if (singlePackageName.isBlank()) return ERROR_SINGLE_PACKAGE_NAME_IS_BLANK

        if ("cf.playhi.freezeyou" == singlePackageName) {
            return ERROR_OPERATION_ON_FREEZEYOU_IS_NOT_ALLOWED
        }

        return when (apiMode) {
            @Suppress("DEPRECATION")
            API_FREEZEYOU_LEGACY_AUTO ->
                pureExecuteAPIAutoAction()
            API_FREEZEYOU_MROOT_DPM ->
                pureExecuteAPIDPMAction()
            API_FREEZEYOU_MROOT_PROFILE_OWNER ->
                pureExecuteAPIProfileOwnerAction()
            API_FREEZEYOU_ROOT_DISABLE_ENABLE ->
                pureExecuteAPIRootAction(false)
            API_FREEZEYOU_ROOT_UNHIDE_HIDE ->
                pureExecuteAPIRootAction(true)
            API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE ->
                pureExecuteAPISystemAppDisabledAction()
            API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED ->
                pureExecuteAPISystemAppDisabledUntilUsedAction()
            API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER ->
                pureExecuteAPISystemAppDisabledUserAction()
            else ->
                ERROR_NO_SUCH_API_MODE
        }
    }

    private fun pureExecuteAPIAutoAction(): Int {
        return if (actionMode == ACTION_MODE_FREEZE) {
            if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
                pureExecuteAPIDPMAction()
            } else {
                pureExecuteAPIRootAction()
            }
        } else {
            if (checkMRootFrozen(context, singlePackageName)) {
                pureExecuteAPIDPMAction()
            } else {
                pureExecuteAPIRootAction()
            }
        }
    }

    private fun pureExecuteAPIDPMAction(): Int {

        if (Build.VERSION.SDK_INT < 21) return ERROR_DEVICE_ANDROID_VERSION_TOO_LOW
        if (!isDeviceOwner(context)) return ERROR_NOT_DEVICE_POLICY_MANAGER

        val hidden = actionMode == ACTION_MODE_FREEZE
        if (!hidden &&
            !getDevicePolicyManager(context)
                .isApplicationHidden(getComponentName(context), singlePackageName)
        ) {
            return ERROR_NO_ERROR_SUCCESS
        }

        return if (getDevicePolicyManager(context).setApplicationHidden(
                getComponentName(context),
                singlePackageName,
                hidden
            )
        ) {
            ERROR_NO_ERROR_SUCCESS
        } else {
            ERROR_DPM_EXECUTE_FAILED_FROM_SYSTEM
        }

    }

    private fun pureExecuteAPIRootAction(): Int {
        return when (apiMode) {
            @Suppress("DEPRECATION")
            API_FREEZEYOU_LEGACY_AUTO, API_FREEZEYOU_ROOT_DISABLE_ENABLE ->
                pureExecuteAPIRootAction(false)
            API_FREEZEYOU_ROOT_UNHIDE_HIDE ->
                pureExecuteAPIRootAction(true)
            else ->
                ERROR_OTHER
        }
    }

    private fun pureExecuteAPIRootAction(hideMode: Boolean): Int {

        var returnValue = ERROR_OTHER
        val enable = actionMode == ACTION_MODE_UNFREEZE
        try {
            val exitValue = fAURoot(singlePackageName, enable, hideMode)
            returnValue = if (exitValue == 0) {
                ERROR_NO_ERROR_SUCCESS
            } else {
                ERROR_OTHER
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val eMsg = e.message
            if (eMsg != null &&
                (eMsg.contains("permission denied", true)
                        || eMsg.contains("not found", true))
            ) {
                returnValue = ERROR_NO_ROOT_PERMISSION
            }
        }
        return returnValue
    }

    private fun pureExecuteAPISystemAppDisabledUntilUsedAction(): Int {

        if (Build.VERSION.SDK_INT < 18) return ERROR_DEVICE_ANDROID_VERSION_TOO_LOW
        if (!isSystemApp(context)) return ERROR_NOT_SYSTEM_APP
        val freeze = actionMode == ACTION_MODE_FREEZE
        context.packageManager.setApplicationEnabledSetting(
            singlePackageName,
            if (freeze)
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED
            else
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            0
        )
        return ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT
    }

    private fun pureExecuteAPISystemAppDisabledUserAction(): Int {

        if (!isSystemApp(context)) return ERROR_NOT_SYSTEM_APP
        val freeze = actionMode == ACTION_MODE_FREEZE
        context.packageManager.setApplicationEnabledSetting(
            singlePackageName,
            if (freeze)
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            else
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            0
        )
        return ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT
    }

    private fun pureExecuteAPISystemAppDisabledAction(): Int {

        if (!isSystemApp(context)) return ERROR_NOT_SYSTEM_APP
        val freeze = actionMode == ACTION_MODE_FREEZE
        context.packageManager.setApplicationEnabledSetting(
            singlePackageName,
            if (freeze)
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            0
        )
        return ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT
    }

    private fun pureExecuteAPIProfileOwnerAction(): Int {

        if (Build.VERSION.SDK_INT < 21) return ERROR_DEVICE_ANDROID_VERSION_TOO_LOW
        if (!isProfileOwner(context)) return ERROR_NOT_PROFILE_OWNER

        val hidden = actionMode == ACTION_MODE_FREEZE
        if (!hidden &&
            !getDevicePolicyManager(context)
                .isApplicationHidden(getComponentName(context), singlePackageName)
        ) {
            return ERROR_NO_ERROR_SUCCESS
        }

        return if (getDevicePolicyManager(context).setApplicationHidden(
                getComponentName(context),
                singlePackageName,
                hidden
            )
        ) {
            ERROR_NO_ERROR_SUCCESS
        } else {
            ERROR_PROFILE_OWNER_EXECUTE_FAILED_FROM_SYSTEM
        }

    }

    companion object {
        const val ACTION_MODE_FREEZE = 0
        const val ACTION_MODE_UNFREEZE = 1
        const val ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT = 1
        const val ERROR_NO_ERROR_SUCCESS = 0
        const val ERROR_OTHER = -1
        const val ERROR_SINGLE_PACKAGE_NAME_IS_BLANK = -2
        const val ERROR_DEVICE_ANDROID_VERSION_TOO_LOW = -3
        const val ERROR_NO_ROOT_PERMISSION = -4
        const val ERROR_DPM_EXECUTE_FAILED_FROM_SYSTEM = -5
        const val ERROR_NOT_DEVICE_POLICY_MANAGER = -6
        const val ERROR_NO_SUCH_API_MODE = -7
        const val ERROR_NOT_SYSTEM_APP = -8
        const val ERROR_NOT_PROFILE_OWNER = -9
        const val ERROR_PROFILE_OWNER_EXECUTE_FAILED_FROM_SYSTEM = -10
        const val ERROR_OPERATION_ON_FREEZEYOU_IS_NOT_ALLOWED = -11
        const val ERROR_USER_SET_NOT_ALLOWED_TO_FREEZE_FOREGROUND_APPLICATION = -12
        const val ERROR_USER_SET_NOT_ALLOWED_TO_FREEZE_NOTIFYING_APPLICATION = -13
        const val ERROR_NO_SUFFICIENT_PERMISSION_TO_START_THIS_ACTIVITY = -14
        const val ERROR_CANNOT_FIND_THE_LAUNCH_INTENT_OR_UNFREEZE_FAILED = -15

        /**
         * 使用 FreezeYou 的 自动（免ROOT(DPM)/ROOT(DISABLE)） 模式
         *
         * 推荐直接使用 [API_FREEZEYOU_MROOT_DPM]
         * 与 [API_FREEZEYOU_ROOT_DISABLE_ENABLE]
         * 与 [API_FREEZEYOU_ROOT_UNHIDE_HIDE]
         */
        @Deprecated("Use API_FREEZEYOU_MROOT_DPM or API_FREEZEYOU_ROOT_DISABLE_ENABLE instead.")
        const val API_FREEZEYOU_LEGACY_AUTO = 0

        /**
         * 使用 FreezeYou 的 免ROOT(DPM) 模式
         */
        const val API_FREEZEYOU_MROOT_DPM = 1

        /**
         * 使用 FreezeYou 的 ROOT(DISABLE) 模式
         */
        const val API_FREEZEYOU_ROOT_DISABLE_ENABLE = 2

        /**
         * 使用 FreezeYou 的 ROOT(UNHIDE) 模式
         */
        const val API_FREEZEYOU_ROOT_UNHIDE_HIDE = 3

        /**
         * 使用 FreezeYou 的 System App (DISABLE_UNTIL_USED) 模式
         */
        const val API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED = 4

        /**
         * 使用 FreezeYou 的 System App (DISABLE_USER) 模式
         */
        const val API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER = 5

        /**
         * 使用 FreezeYou 的 System App (DISABLE) 模式
         */
        const val API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE = 6

        /**
         * 使用 FreezeYou 的 免ROOT (Profile Owner) 模式
         */
        const val API_FREEZEYOU_MROOT_PROFILE_OWNER = 7

    }
}