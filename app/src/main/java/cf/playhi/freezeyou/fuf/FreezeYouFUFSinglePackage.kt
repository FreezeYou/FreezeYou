package cf.playhi.freezeyou.fuf

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.annotation.UiThread
import cf.playhi.freezeyou.MainApplication
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.avoidFreezeForegroundApplications
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.openImmediately
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.selectFUFMode
import cf.playhi.freezeyou.ui.AskRunActivity
import cf.playhi.freezeyou.utils.FUFUtils.checkAndCreateFUFQuickNotification
import cf.playhi.freezeyou.utils.FUFUtils.isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying
import cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus
import cf.playhi.freezeyou.utils.FUFUtils.sendStatusChangedBroadcast
import cf.playhi.freezeyou.utils.NotificationUtils.deleteNotification
import cf.playhi.freezeyou.utils.TasksUtils.*
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FreezeYouFUFSinglePackage(
    override val context: Context,
    override val singlePackageName: String,
    override val actionMode: Int = if (
        realGetFrozenStatus(
            context,
            singlePackageName
        )
    ) ACTION_MODE_UNFREEZE else ACTION_MODE_FREEZE,
    override val apiMode: Int = selectFUFMode.getValue()!!.toInt(),
    val needAskRun: Boolean = false,
    /**
     * Only takes effect when [needAskRun] is true
     */
    val runImmediately: Boolean = false,
    val tasks: String? = null,
    val target: String? = null
) : FUFSinglePackage(context, singlePackageName, actionMode, apiMode) {

    /**
     * In most cases, we should call [checkAndStartTaskAndTargetAndActivity] after the method returns.
     */
    override suspend fun commit(): Int {
        if ("cf.playhi.freezeyou" == singlePackageName) {
            return ERROR_OPERATION_ON_FREEZEYOU_IS_NOT_ALLOWED
        }
        if (actionMode == ACTION_MODE_FREEZE) {
            if (avoidFreezeForegroundApplications.getValue()
                && MainApplication.currentPackage.equals(singlePackageName)
            ) {
                return ERROR_USER_SET_NOT_ALLOWED_TO_FREEZE_FOREGROUND_APPLICATION
            }
            if (isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(singlePackageName)) {
                return ERROR_USER_SET_NOT_ALLOWED_TO_FREEZE_NOTIFYING_APPLICATION
            }
        }
        val result = super.commit()
        when (result) {
            ERROR_NO_ERROR_SUCCESS, ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT -> {
                sendStatusChangedBroadcast(context)
                when (actionMode) {
                    ACTION_MODE_FREEZE -> {
                        withContext(Dispatchers.Main) {
                            onFApplications(context, singlePackageName)
                        }
                        deleteNotification(context, singlePackageName)
                    }
                    ACTION_MODE_UNFREEZE -> {
                        withContext(Dispatchers.Main) {
                            onUFApplications(context, singlePackageName)
                        }
                        checkAndCreateFUFQuickNotification(context, singlePackageName)
                    }
                }
            }
        }
        return result
    }

    /**
     * As [runTask] contains [showToast] related function,
     * this method should run on UI thread.
     */
    @UiThread
    fun checkAndStartTaskAndTargetAndActivity(originResult: Int): Int {
        if (originResult != ERROR_NO_ERROR_SUCCESS
            && originResult != ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT
        ) {
            return originResult
        }

        if (tasks != null) {
            runTask(tasks, context, null)
        }

        if (needAskRun && context.getString(R.string.onlyUnfreeze) != target) {
            if (runImmediately || openImmediately.getValue()) {
                checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(
                    context,
                    singlePackageName,
                    target,
                    null, // Already executed
                    originResult
                ).let { if (it != originResult) return it }
            } else {
                context.startActivity(
                    Intent(context, AskRunActivity::class.java)
                        .putExtra("pkgName", singlePackageName)
                        .putExtra("target", target)
                        .putExtra("tasks", tasks)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
        return originResult
    }

    companion object {

        /**
         * As [runTask] contains [showToast] related function,
         * this method should run on UI thread.
         */
        @UiThread
        fun checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(
            context: Context,
            pkgName: String,
            target: String?,
            tasks: String?,
            originResult: Int = ERROR_NO_ERROR_SUCCESS
        ): Int {

            if (tasks != null) {
                runTask(tasks, context, null)
            }

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
                        return ERROR_NO_SUFFICIENT_PERMISSION_TO_START_THIS_ACTIVITY
                    }
                }
            } else if (
                context.packageManager.getLaunchIntentForPackage(pkgName) != null
            ) {
                val intent = Intent(
                    context.packageManager.getLaunchIntentForPackage(
                        pkgName
                    )
                )
                context.startActivity(intent)
            } else {
                return ERROR_CANNOT_FIND_THE_LAUNCH_INTENT_OR_UNFREEZE_FAILED
            }
            return originResult
        }

    }

}