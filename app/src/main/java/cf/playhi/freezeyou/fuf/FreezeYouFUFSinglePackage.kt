package cf.playhi.freezeyou.fuf

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import cf.playhi.freezeyou.MainApplication
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.avoidFreezeForegroundApplications
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.openImmediately
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.selectFUFMode
import cf.playhi.freezeyou.ui.AskRunActivity
import cf.playhi.freezeyou.utils.FUFUtils.*
import cf.playhi.freezeyou.utils.NotificationUtils.deleteNotification
import cf.playhi.freezeyou.utils.TasksUtils.*

class FreezeYouFUFSinglePackage(
    context: Context,
    packageName: String,
    actionMode: Int = if (
        realGetFrozenStatus(
            context,
            packageName
        )
    ) ACTION_MODE_UNFREEZE else ACTION_MODE_FREEZE,
    apiMode: Int = selectFUFMode.getValue()!!.toInt(),
    askRun: Boolean = false,
    /**
     * Only takes effect when askRun is true
     */
    runImmediately: Boolean = false,
    tasks: String? = null,
    target: String? = null
) : FUFSinglePackage(context, packageName, actionMode, apiMode) {

    private val mAskRun = askRun
    private val mRunImmediately = runImmediately
    private val mTasks = tasks
    private val mTarget = target

    override fun commit(): Int {
        if ("cf.playhi.freezeyou" == mSinglePackageName) {
            // TODO:
            return -100//
        }
        if (mActionMode == ACTION_MODE_FREEZE) {
            if (avoidFreezeForegroundApplications.getValue()
                && MainApplication.currentPackage.equals(mSinglePackageName)
            ) {
                // TODO:
                return -999//
            }
            if (isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(mSinglePackageName)) {
                // TODO:
                return -999//
            }
        }
        val result = super.commit()
        when (result) {
            ERROR_NO_ERROR_SUCCESS, ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT -> {
                sendStatusChangedBroadcast(mContext)
                when (mActionMode) {
                    ACTION_MODE_FREEZE -> {
                        onFApplications(mContext, mSinglePackageName)
                        deleteNotification(mContext, mSinglePackageName)
                    }
                    ACTION_MODE_UNFREEZE -> {
                        onUFApplications(mContext, mSinglePackageName)
                        checkAndCreateFUFQuickNotification(mContext, mSinglePackageName)
                        if (mContext.getString(R.string.onlyUnfreeze) != mTarget && mAskRun) {
                            if (mRunImmediately || openImmediately.getValue()) {
                                if (mTasks != null) {
                                    runTask(mTasks, mContext, null)
                                }
                                if (mTarget != null) {
                                    try {
                                        val component = ComponentName(mSinglePackageName, mTarget)
                                        val intent = Intent()
                                        intent.component = component
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        intent.action = Intent.ACTION_MAIN
                                        mContext.startActivity(intent)
                                    } catch (e: SecurityException) {
                                        e.printStackTrace()
                                        // TODO:
                                        return -888//success, insufficientPermission
//                                        showToast(context, R.string.insufficientPermission)
                                    }
                                } else if (mContext.packageManager
                                        .getLaunchIntentForPackage(mSinglePackageName) != null
                                ) {
                                    val intent = Intent(
                                        mContext.packageManager.getLaunchIntentForPackage(
                                            mSinglePackageName
                                        )
                                    )
                                    mContext.startActivity(intent)
                                } else {
                                    // TODO:
                                    return -777//success, unrootedOrCannotFindTheLaunchIntent
//                                    showToast(
//                                        context,
//                                        R.string.unrootedOrCannotFindTheLaunchIntent
//                                    )
                                }
                            } else {
                                mContext.startActivity(
                                    Intent(mContext, AskRunActivity::class.java)
                                        .putExtra("pkgName", mSinglePackageName)
                                        .putExtra("target", mTarget)
                                        .putExtra("tasks", mTasks)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    }
                }
            }
        }
        return result
    }

}