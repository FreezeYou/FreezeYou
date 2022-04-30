package cf.playhi.freezeyou.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import cf.playhi.freezeyou.DeviceAdminReceiver
import cf.playhi.freezeyou.MainApplication
import cf.playhi.freezeyou.MyNotificationListenerService
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.fuf.FUFSinglePackage
import cf.playhi.freezeyou.service.FUFService
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.lesserToast
import cf.playhi.freezeyou.ui.AskRunActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.DataOutputStream
import java.util.*

object FUFUtils {
    @JvmStatic
    fun askRun(
        context: Context,
        pkgName: String,
        target: String?,
        tasks: String?,
        runImmediately: Boolean,
        activity: Activity?,
        finish: Boolean
    ) {
        if (runImmediately || DefaultMultiProcessMMKVStorageBooleanKeys.openImmediately.getValue(
                null
            )
        ) {
            checkAndStartApp(context, pkgName, target, tasks, activity, finish)
        } else {
            if (context.getString(R.string.onlyUnfreeze) != target) {
                context.startActivity(
                    Intent(context, AskRunActivity::class.java)
                        .putExtra("pkgName", pkgName)
                        .putExtra("target", target)
                        .putExtra("tasks", tasks)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    @JvmStatic
    fun checkAndDoActivityFinish(activity: Activity?, finish: Boolean) {
        if (activity != null && finish) {
            activity.finish()
        }
    }

    /**
     * @param context              Context
     * @param pkgName              PkgName
     * @param apiMode              ApiMode
     * @param enable               Enable
     * @param showUnnecessaryToast ShowUnnecessaryToast
     * @param askRun               AskRun
     * @param target               Target, askRun 为 true 时使用
     * @param tasks                Tasks, askRun 为 true 时使用
     * @param runImmediately       RunImmediately, askRun 为 true 时使用
     * @param activity             Activity, askRun 为 true 时使用
     * @param finish               Finish, askRun 为 true 时使用
     * @return boolean
     */
    @JvmStatic
    @JvmOverloads
    fun processAction(
        context: Context, pkgName: String, apiMode: Int,
        enable: Boolean, showUnnecessaryToast: Boolean,
        askRun: Boolean = false, target: String? = null, tasks: String? = null,
        runImmediately: Boolean = false, activity: Activity? = null, finish: Boolean = false
    ): Boolean {
        val result = checkAndExecuteAction(
            context, pkgName,
            apiMode,
            if (enable) FUFSinglePackage.ACTION_MODE_UNFREEZE else FUFSinglePackage.ACTION_MODE_FREEZE
        )
        if (preProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                context, result, showUnnecessaryToast
            )
        ) {
            sendStatusChangedBroadcast(context)
            if (enable) {
                TasksUtils.onUFApplications(context, pkgName)
                checkAndCreateFUFQuickNotification(context, pkgName)
                if (askRun) {
                    askRun(context, pkgName, target, tasks, runImmediately, activity, finish)
                }
            } else {
                TasksUtils.onFApplications(context, pkgName)
                NotificationUtils.deleteNotification(context, pkgName)
            }
            return true
        }
        return false
    }

    @JvmStatic
    fun processRootAction(
        pkgName: String, target: String?, tasks: String?, context: Context,
        enable: Boolean, askRun: Boolean, runImmediately: Boolean, activity: Activity?,
        finish: Boolean, showUnnecessaryToast: Boolean
    ): Boolean {
        return processAction(
            context,
            pkgName,
            FUFSinglePackage.API_FREEZEYOU_ROOT_DISABLE_ENABLE,
            enable,
            showUnnecessaryToast,
            askRun,
            target,
            tasks,
            runImmediately,
            activity,
            finish
        )
    }

    @JvmStatic
    @TargetApi(21)
    fun processMRootAction(
        context: Context, pkgName: String, target: String?, tasks: String?, hidden: Boolean,
        askRun: Boolean, runImmediately: Boolean, activity: Activity?,
        finish: Boolean, showUnnecessaryToast: Boolean
    ): Boolean {
        return processAction(
            context,
            pkgName,
            FUFSinglePackage.API_FREEZEYOU_MROOT_DPM,
            !hidden,
            showUnnecessaryToast,
            askRun,
            target,
            tasks,
            runImmediately,
            activity,
            finish
        )
    }

    @JvmStatic
    fun checkAndExecuteAction(
        context: Context,
        pkgName: String,
        apiMode: Int,
        actionMode: Int
    ): Int {
        var returnValue = 999
        var currentPackage: String? = " "
        if (DefaultMultiProcessMMKVStorageBooleanKeys.avoidFreezeForegroundApplications.getValue(
                null
            )
        ) {
            currentPackage = MainApplication.currentPackage
        }
        if (currentPackage == null) currentPackage = " "
        if ("cf.playhi.freezeyou" != pkgName) {
            if (actionMode == FUFSinglePackage.ACTION_MODE_FREEZE &&
                isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(pkgName)
            ) {
                checkAndShowAppStillNotifyingToast(context, pkgName)
            } else if (actionMode == FUFSinglePackage.ACTION_MODE_FREEZE && currentPackage == pkgName) {
                checkAndShowAppIsForegroundApplicationToast(context, pkgName)
            } else {
                runBlocking {
                    launch {
                        returnValue =
                            FUFSinglePackage(context, pkgName, actionMode, apiMode).commit()
                    }.join()
                }
            }
        }
        return returnValue
    }

    @JvmStatic
    fun checkAndStartApp(
        context: Context,
        pkgName: String,
        target: String?,
        tasks: String?,
        activity: Activity?,
        finish: Boolean
    ) {
        if (target != null) {
            if (context.getString(R.string.onlyUnfreeze) != target) {
                try {
                    val component = ComponentName(pkgName, target)
                    val intent = Intent()
                    intent.component = component
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = Intent.ACTION_MAIN
                    context.startActivity(intent)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    ToastUtils.showToast(context, R.string.insufficientPermission)
                }
            }
        } else if (context.packageManager.getLaunchIntentForPackage(pkgName) != null) {
            val intent = Intent(
                context.packageManager.getLaunchIntentForPackage(pkgName)
            )
            context.startActivity(intent)
        } else {
            ToastUtils.showToast(
                context,
                R.string.unrootedOrCannotFindTheLaunchIntent
            )
        }
        if (tasks != null) {
            TasksUtils.runTask(tasks, context, null)
        }
        checkAndDoActivityFinish(activity, finish)
    }

    @JvmStatic
    @JvmOverloads
    fun oneKeyActionRoot(
        context: Context, freeze: Boolean, pkgNameList: Array<String>?,
        disableModeTrueOrHideModeFalse: Boolean = true
    ) {
        if (pkgNameList != null) {
            var currentPackage: String? = " "
            if (DefaultMultiProcessMMKVStorageBooleanKeys.avoidFreezeForegroundApplications.getValue(
                    null
                )
            ) {
                currentPackage = MainApplication.currentPackage
            }
            if (currentPackage == null) currentPackage = " "
            var process: Process? = null
            var outputStream: DataOutputStream? = null
            try {
                process = Runtime.getRuntime().exec("su")
                outputStream = DataOutputStream(process.outputStream)
                if (freeze) {
                    for (aPkgNameList in pkgNameList) {
                        if ("cf.playhi.freezeyou" != aPkgNameList) {
                            if (isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(
                                    aPkgNameList
                                )
                            ) {
                                checkAndShowAppStillNotifyingToast(context, aPkgNameList)
                            } else if (currentPackage == aPkgNameList) {
                                checkAndShowAppIsForegroundApplicationToast(context, aPkgNameList)
                            } else {
                                try {
                                    val tmp = context.packageManager.getApplicationEnabledSetting(
                                        aPkgNameList
                                    )
                                    if (tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER && tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED && tmp != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                                        outputStream.writeBytes(
                                            """
                                                pm ${if (disableModeTrueOrHideModeFalse) "disable " else "hide "}$aPkgNameList
                                                
                                                """.trimIndent()
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    //                                    if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
//                                        showToast(context, R.string.plsRemoveUninstalledApplications);
//                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (aPkgNameList in pkgNameList) {
                        try {
                            if (disableModeTrueOrHideModeFalse) {
                                val tmp =
                                    context.packageManager.getApplicationEnabledSetting(aPkgNameList)
                                if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                                    outputStream.writeBytes("pm enable $aPkgNameList\n")
                                }
                            } else {
                                outputStream.writeBytes("pm unhide $aPkgNameList\n")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            //                            if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
//                                showToast(context, R.string.plsRemoveUninstalledApplications);
//                            }
                        }
                    }
                }
                outputStream.writeBytes("exit\n")
                outputStream.flush()
                val exitValue = process.waitFor()
                if (exitValue == 0) {
                    if (freeze) {
                        for (aPkgNameList in pkgNameList) {
                            TasksUtils.onFApplications(context, aPkgNameList)
                            NotificationUtils.deleteNotification(context, aPkgNameList)
                        }
                    } else {
                        for (aPkgNameList in pkgNameList) {
                            TasksUtils.onUFApplications(context, aPkgNameList)
                            checkAndCreateFUFQuickNotification(context, aPkgNameList)
                        }
                    }
                    if (!lesserToast.getValue(null)) {
                        ToastUtils.showToast(context, R.string.executed)
                    }
                } else {
                    ToastUtils.showToast(context, R.string.mayUnrootedOrOtherEx)
                }
                ProcessUtils.destroyProcess(outputStream, process)
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtils.showToast(context, context.getString(R.string.exception) + e.message)
                if (e.message!!.lowercase(Locale.getDefault())
                        .contains("permission denied") || e.message!!.lowercase(
                        Locale.getDefault()
                    ).contains("not found")
                ) {
                    ToastUtils.showToast(context, R.string.mayUnrooted)
                }
                ProcessUtils.destroyProcess(outputStream, process)
            }
            sendStatusChangedBroadcast(context)
        }
    }

    @JvmStatic
    @TargetApi(21)
    @Deprecated(
        "DEPRECATED", ReplaceWith(
            "oneKeyAction(context, freeze, pkgNameList, FUFSinglePackage.API_FREEZEYOU_MROOT_DPM)",
            "cf.playhi.freezeyou.utils.FUFUtils.oneKeyAction",
            "cf.playhi.freezeyou.fuf.FUFSinglePackage"
        )
    )
    fun oneKeyActionMRoot(context: Context, freeze: Boolean, pkgNameList: Array<String>?) {
        oneKeyAction(context, freeze, pkgNameList, FUFSinglePackage.API_FREEZEYOU_MROOT_DPM)
    }

    @JvmStatic
    fun oneKeyAction(context: Context, freeze: Boolean, pkgNameList: Array<String>?, apiMode: Int) {
        when (apiMode) {
            FUFSinglePackage.API_FREEZEYOU_ROOT_DISABLE_ENABLE -> oneKeyActionRoot(
                context,
                freeze,
                pkgNameList,
                true
            )
            FUFSinglePackage.API_FREEZEYOU_ROOT_UNHIDE_HIDE -> oneKeyActionRoot(
                context,
                freeze,
                pkgNameList,
                false
            )
            else -> if (pkgNameList != null) {
                for (aPkgName in pkgNameList) {
                    try {
                        @Suppress("DEPRECATION")
                        if ("cf.playhi.freezeyou" != aPkgName &&
                            apiMode != FUFSinglePackage.API_FREEZEYOU_LEGACY_AUTO &&
                            apiMode != FUFSinglePackage.API_FREEZEYOU_MROOT_DPM ||
                            !freeze || !checkMRootFrozen(context, aPkgName)
                        ) {
                            if (!processAction(context, aPkgName, apiMode, !freeze, false)) {
                                ToastUtils.showToast(
                                    context,
                                    aPkgName + " " + context.getString(R.string.failed) + " " + context.getString(
                                        R.string.mayUnrootedOrOtherEx
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ToastUtils.showToast(
                            context,
                            context.getString(R.string.exceptionHC) + e.localizedMessage
                        )
                    }
                }
                sendStatusChangedBroadcast(context)
                if (!lesserToast.getValue(null)) {
                    ToastUtils.showToast(context, R.string.executed)
                }
            }
        }
    }

    fun sendStatusChangedBroadcast(context: Context) {
        val intent = Intent()
        intent.action = "cf.playhi.freezeyou.action.packageStatusChanged"
        intent.setPackage("cf.playhi.freezeyou")
        context.sendBroadcast(intent)
    }

    @TargetApi(21)
    private fun isAppStillNotifying(pkgName: String?): Boolean {
        if (pkgName != null) {
            val statusBarNotifications = MyNotificationListenerService.getStatusBarNotifications()
            if (statusBarNotifications != null) {
                for (aStatusBarNotifications in statusBarNotifications) {
                    if (pkgName == aStatusBarNotifications.packageName) {
                        return true
                    }
                }
            }
        }
        return false
    }

    @JvmStatic
    fun isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(pkgName: String?): Boolean {
        return if (Build.VERSION.SDK_INT >= 21) {
            DefaultMultiProcessMMKVStorageBooleanKeys.avoidFreezeNotifyingApplications.getValue(null) && isAppStillNotifying(
                pkgName
            )
        } else {
            false
        }
    }

    @JvmStatic
    fun checkAndShowAppStillNotifyingToast(context: Context, pkgName: String?) {
        val label = ApplicationLabelUtils.getApplicationLabel(
            context,
            null,
            null,
            pkgName
        )
        if (context.getString(R.string.uninstalled) != label) ToastUtils.showToast(
            context, String.format(
                context.getString(R.string.appHasNotifi),
                label
            )
        )
    }

    @JvmStatic
    fun checkAndShowAppIsForegroundApplicationToast(context: Context, pkgName: String?) {
        val label = ApplicationLabelUtils.getApplicationLabel(
            context,
            null,
            null,
            pkgName
        )
        if (context.getString(R.string.uninstalled) != label) ToastUtils.showToast(
            context, String.format(
                context.getString(R.string.isForegroundApplication),
                label
            )
        )
    }

    @JvmStatic
    fun processUnfreezeAction(
        context: Context?,
        pkgName: String?,
        target: String?,
        tasks: String?,
        askRun: Boolean,
        runImmediately: Boolean,
        activity: Activity?,
        finish: Boolean
    ) {
        ServiceUtils.startService(
            context, Intent(context, FUFService::class.java)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("freeze", false)
                .putExtra("target", target) //目标 Activity
                .putExtra("tasks", tasks) //追加任务
                .putExtra("single", true)
                .putExtra("runImmediately", runImmediately)
        )
        checkAndDoActivityFinish(activity, finish)
    }

    @JvmStatic
    fun processFreezeAction(
        context: Context?,
        pkgName: String?,
        target: String?,
        tasks: String?,
        askRun: Boolean,
        activity: Activity?,
        finish: Boolean
    ) {
        ServiceUtils.startService(
            context, Intent(context, FUFService::class.java)
                .putExtra("askRun", askRun)
                .putExtra("pkgName", pkgName)
                .putExtra("target", target) //目标 Activity
                .putExtra("tasks", tasks) //追加任务
                .putExtra("freeze", true)
                .putExtra("single", true)
        )
        checkAndDoActivityFinish(activity, finish)
    }

    @JvmStatic
    fun checkMRootFrozen(context: Context?, pkgName: String): Boolean {
        return try {
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (DevicePolicyManagerUtils.isDeviceOwner(
                context
            ) || DevicePolicyManagerUtils.isProfileOwner(context))
                    && DevicePolicyManagerUtils.getDevicePolicyManager(context)
                .isApplicationHidden(DeviceAdminReceiver.getComponentName(context), pkgName))
        } catch (e: Exception) {
            false
        }
    }

    @JvmStatic
    fun checkRootFrozen(
        context: Context,
        pkgName: String,
        packageManager: PackageManager?
    ): Boolean {
        return try {
            packageManager?.getApplicationEnabledSetting(pkgName)
                ?: context.packageManager.getApplicationEnabledSetting(
                    pkgName
                )
        } catch (e: Exception) {
            -1
        }.let {
            it != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT &&
                    it != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
    }

    /**
     * @param packageName 应用包名
     * @return true 则已冻结
     */
    @JvmStatic
    fun realGetFrozenStatus(context: Context, packageName: String): Boolean {
        return (checkMRootFrozen(context, packageName)
                || checkRootFrozen(context, packageName, context.packageManager))
    }

    /**
     * @param packageName 应用包名
     * @return true 则已冻结
     */
    @JvmStatic
    fun realGetFrozenStatus(context: Context, packageName: String, pm: PackageManager?): Boolean {
        return checkRootFrozen(context, packageName, pm) || checkMRootFrozen(context, packageName)
    }

    @JvmStatic
    fun checkFrozenStatusAndStartApp(
        context: Context,
        pkgName: String,
        target: String?,
        tasks: String?
    ) {
        if (realGetFrozenStatus(context, pkgName, null)) {
            processUnfreezeAction(
                context, pkgName, target, tasks,
                askRun = true,
                runImmediately = true,
                activity = null,
                finish = false
            )
        } else {
            checkAndStartApp(context, pkgName, target, tasks, null, false)
        }
    }

    @JvmStatic
    fun checkRootPermission(): Boolean {
        var hasPermission = true
        var value = -1
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            value = process.waitFor()
            try {
                outputStream.close()
                process.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            if (e.message!!.lowercase(Locale.getDefault())
                    .contains("permission denied") || e.message!!.lowercase(
                    Locale.getDefault()
                ).contains("not found")
            ) {
                hasPermission = false
            }
        }
        return hasPermission && value == 0
    }

    @JvmStatic
    fun checkAndCreateFUFQuickNotification(context: Context?, pkgName: String?) {
        if (DefaultMultiProcessMMKVStorageBooleanKeys.createQuickFUFNotiAfterUnfrozen.getValue(null)) {
            NotificationUtils.createFUFQuickNotification(
                context, pkgName, R.drawable.ic_notification,
                ApplicationIconUtils.getBitmapFromDrawable(
                    ApplicationIconUtils.getApplicationIcon(
                        context!!,
                        pkgName!!,
                        ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context),
                        false
                    )
                )
            )
        }
    }

    fun isSystemApp(context: Context): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
    }

    fun preProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
        context: Context, resultCode: Int, showUnnecessaryToast: Boolean
    ): Boolean {
        return when (resultCode) {
            FUFSinglePackage.ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT -> {
                if (showUnnecessaryToast) showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.unknownResult_probablySuccess)
                )
                true
            }
            FUFSinglePackage.ERROR_NO_ERROR_SUCCESS -> {
                if (showUnnecessaryToast) showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.success)
                )
                true
            }
            FUFSinglePackage.ERROR_SINGLE_PACKAGE_NAME_IS_BLANK -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.packageNameIsEmpty)
                )
                false
            }
            FUFSinglePackage.ERROR_DEVICE_ANDROID_VERSION_TOO_LOW -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.sysVerLow)
                )
                false
            }
            FUFSinglePackage.ERROR_NO_ROOT_PERMISSION -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.noRootPermission)
                )
                false
            }
            FUFSinglePackage.ERROR_DPM_EXECUTE_FAILED_FROM_SYSTEM, FUFSinglePackage.ERROR_PROFILE_OWNER_EXECUTE_FAILED_FROM_SYSTEM -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.executeFailedFromSystem)
                )
                false
            }
            FUFSinglePackage.ERROR_NOT_DEVICE_POLICY_MANAGER -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.isNotDevicePolicyManager)
                )
                false
            }
            FUFSinglePackage.ERROR_NOT_PROFILE_OWNER -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.isNotProfileOwner)
                )
                false
            }
            FUFSinglePackage.ERROR_NO_SUCH_API_MODE -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.noSuchApiMode)
                )
                false
            }
            FUFSinglePackage.ERROR_NOT_SYSTEM_APP -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.isNotSystemApp)
                )
                false
            }
            FUFSinglePackage.ERROR_OTHER -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.unknownError)
                )
                false
            }
            else -> {
                showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                    context, context.getString(R.string.unknownError)
                )
                false
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getFUFRelatedToastString(
        context: Context,
        resultCode: Int,
        showUnnecessaryToast: Boolean? = null
    ): String? {
        return when (resultCode) {
            FUFSinglePackage.ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT -> {
                if (showUnnecessaryToast == false || lesserToast.getValue(null))
                    null
                else
                    getPreProcessedFUFResultToastString(
                        context, context.getString(R.string.unknownResult_probablySuccess)
                    )
            }
            FUFSinglePackage.ERROR_NO_ERROR_SUCCESS -> {
                if (showUnnecessaryToast == false || lesserToast.getValue(null))
                    null
                else
                    getPreProcessedFUFResultToastString(
                        context, context.getString(R.string.success)
                    )
            }
            FUFSinglePackage.ERROR_SINGLE_PACKAGE_NAME_IS_BLANK ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.packageNameIsEmpty)
                )
            FUFSinglePackage.ERROR_DEVICE_ANDROID_VERSION_TOO_LOW ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.sysVerLow)
                )
            FUFSinglePackage.ERROR_NO_ROOT_PERMISSION ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.noRootPermission)
                )
            FUFSinglePackage.ERROR_DPM_EXECUTE_FAILED_FROM_SYSTEM,
            FUFSinglePackage.ERROR_PROFILE_OWNER_EXECUTE_FAILED_FROM_SYSTEM ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.executeFailedFromSystem)
                )
            FUFSinglePackage.ERROR_NOT_DEVICE_POLICY_MANAGER ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.isNotDevicePolicyManager)
                )
            FUFSinglePackage.ERROR_NOT_PROFILE_OWNER ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.isNotProfileOwner)
                )
            FUFSinglePackage.ERROR_NO_SUCH_API_MODE ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.noSuchApiMode)
                )
            FUFSinglePackage.ERROR_NOT_SYSTEM_APP ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.isNotSystemApp)
                )
            FUFSinglePackage.ERROR_OPERATION_ON_FREEZEYOU_IS_NOT_ALLOWED ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.operationOnFreezeYouIsNotAllowed)
                )
            FUFSinglePackage.ERROR_USER_SET_NOT_ALLOWED_TO_FREEZE_FOREGROUND_APPLICATION ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.theAppIsCurrentlyTheForegroundApp)
                )
            FUFSinglePackage.ERROR_USER_SET_NOT_ALLOWED_TO_FREEZE_NOTIFYING_APPLICATION ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.theAppHasNotificationInTheNotificationBar)
                )
            FUFSinglePackage.ERROR_NO_SUFFICIENT_PERMISSION_TO_START_THIS_ACTIVITY ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.insufficientPermission)
                )
            FUFSinglePackage.ERROR_CANNOT_FIND_THE_LAUNCH_INTENT_OR_UNFREEZE_FAILED ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.unrootedOrCannotFindTheLaunchIntent)
                )
            FUFSinglePackage.ERROR_OTHER ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.unknownError)
                )
            else ->
                getPreProcessedFUFResultToastString(
                    context, context.getString(R.string.unknownError)
                )
        }
    }

    private fun getPreProcessedFUFResultToastString(context: Context, message: String): String {
        return String.format(context.getString(R.string.executionResult_colon_message), message)
    }

    private fun showPreProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
        context: Context,
        message: String
    ) {
        ToastUtils.showShortToast(
            context,
            String.format(context.getString(R.string.executionResult_colon_message), message)
        )
    }
}